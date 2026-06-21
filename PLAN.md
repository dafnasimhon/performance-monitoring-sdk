# PerfSDK — Android Performance Monitoring SDK

## Context

The goal is to build a production-quality Android SDK that passively collects performance data from Android apps (startup time, screen load times, network latency, custom traces) and ships it to a backend for aggregation and display on a React dashboard.

The project already has a working two-module Android Studio project (`app` + `perfsdk`) with a minimal `PerfSDK` object that measures elapsed time and logs to Logcat. The task is to evolve it phase by phase into a full-stack observability platform.

---

## Architecture Overview

```
Android Application
        │
        ▼
PerfSDK (Kotlin object — public API)
        │
        ├── TraceManager (ConcurrentHashMap of active traces)
        ├── DeviceInfoCollector (Build constants + ConnectivityManager)
        │
        ▼
EventRepository (abstraction layer)
        │
        ├── Phase 1: CopyOnWriteArrayList (in-memory)
        └── Phase 2+: Room Database (EventDao / EventEntity)
        │
        ▼
BatchSender (Retrofit + coroutines)
        │
        ├── Triggered by: queue size ≥ 20 │ 30-sec timer │ flush() │ WorkManager
        │
        ▼
FastAPI Backend  →  MongoDB  →  React Dashboard
```

---

## Technology Stack

| Layer | Technology |
|---|---|
| Android SDK | Kotlin |
| Local storage | Room Database |
| Background upload | WorkManager |
| HTTP client | Retrofit 2 + OkHttp |
| Async | Kotlin Coroutines |
| Backend | Python + FastAPI |
| Database | MongoDB |
| Frontend | React |
| Data format | JSON |

---

## File Structure (perfsdk module)

```
perfsdk/src/main/java/com/example/perfsdk/
├── PerfSDK.kt                        ← public entry point (singleton object)
├── config/
│   └── PerfSDKConfig.kt              ← holds apiKey, context, flags
├── model/
│   ├── EventType.kt                  ← enum: APP_STARTUP, SCREEN_LOAD, NETWORK_REQUEST, CUSTOM_TRACE
│   ├── PerformanceEvent.kt           ← data class for one measurement
│   └── DeviceInfo.kt                 ← value class: model, OS, network type
├── trace/
│   └── TraceManager.kt               ← owns activeTraces ConcurrentHashMap
├── device/
│   └── DeviceInfoCollector.kt        ← reads Build.* + ConnectivityManager
├── storage/
│   ├── EventEntity.kt                ← Room entity (Phase 2)
│   ├── EventDao.kt                   ← Room DAO (Phase 2)
│   ├── PerfDatabase.kt               ← Room database (Phase 2)
│   └── EventRepository.kt            ← Phase 1: in-memory list; Phase 2: Room
├── network/
│   ├── PerfApiService.kt             ← Retrofit interface (Phase 3)
│   ├── RetrofitProvider.kt           ← OkHttp + Retrofit singleton (Phase 3)
│   └── BatchSender.kt                ← batching logic + HTTP call (Phase 3)
├── worker/
│   └── EventUploadWorker.kt          ← WorkManager CoroutineWorker (Phase 4)
└── util/
    ├── DataSanitizer.kt              ← strips PII from event fields
    └── NetworkUtils.kt               ← returns WIFI / CELLULAR / NO_INTERNET / UNKNOWN
```

---

## Phase-by-Phase Plan

### Phase 1 — Basic SDK + In-Memory Queue (CURRENT)

**Files to create:**
- `model/EventType.kt` — enum with 4 values
- `model/PerformanceEvent.kt` — data class matching the full schema (device fields default to Build constants for now)

**Files to modify:**
- `PerfSDK.kt` — add `CopyOnWriteArrayList<PerformanceEvent>` as the event queue; update `stopTrace()` to build a `PerformanceEvent` and add it; add `getPendingEvents(): List<PerformanceEvent>`; guard `startTrace()` against duplicate starts with a log warning instead of crash

**Key decisions:**
- Use `CopyOnWriteArrayList` (thread-safe reads, snapshot via `toList()`)
- `eventType` for `stopTrace` is `CUSTOM_TRACE`; screen traces will use `SCREEN_LOAD` in Phase 5
- Device fields (model, OS version) populated directly from `android.os.Build` — `DeviceInfoCollector` comes in Phase 5
- No `init()` required yet — keep SDK stateless for now

---

### Phase 2 — Room Persistent Storage

**New files:** `EventEntity`, `EventDao`, `PerfDatabase`, `EventRepository`

`EventEntity` mirrors `PerformanceEvent` with an added `uploadStatus` column (`PENDING` / `SENDING` / `FAILED`).

`EventRepository` swaps the in-memory list for Room calls. `PerfSDK` writes through `EventRepository` using a background coroutine (`Dispatchers.IO`).

`PerfSDK.init(context, apiKey)` is introduced here to build the Room instance.

---

### Phase 3 — Backend Communication

**New files:** `PerfApiService`, `RetrofitProvider`, `BatchSender`

`BatchSender` reads up to 20 `PENDING` events from Room, marks them `SENDING`, POSTs them as JSON, deletes on success or marks `FAILED` on error.

FastAPI endpoint: `POST /api/v1/events/batch` — reads `X-API-Key` header, validates, inserts raw events into MongoDB `raw_events` collection.

---

### Phase 4 — Reliable Upload via WorkManager

**New file:** `EventUploadWorker`

Periodic `PeriodicWorkRequest` (every 15 min minimum, constraint: network available).

`flush()` triggers an immediate `OneTimeWorkRequest`.

Retry logic uses WorkManager's built-in exponential backoff.

---

### Phase 5 — Additional Measurements

Add `startScreenTrace` / `stopScreenTrace`, `trackNetworkCall`, `DeviceInfoCollector`, `NetworkUtils`.

App startup time measured by calling `startTrace("APP_STARTUP")` in `Application.onCreate()` and `stopTrace` in `Activity.onResume()`.

---

### Phase 6 — Aggregation

FastAPI background jobs (APScheduler or Celery) aggregate `raw_events` into `aggregated_metrics` by hour and day. Indexes on `appId + timestamp` family of compound indexes.

---

### Phase 7 — React Dashboard

React app with Recharts. Sections: KPI cards, Startup Trend chart, Slow Screens table, Slow Endpoints table, Custom Traces table. Filters: app version, Android version, device model, network type, time range.

---

## Verification (Phase 1)

1. Build the project in Android Studio — zero compile errors.
2. Run the app on emulator/device.
3. In Logcat filter by tag `PerfSDK`:
   - `Trace started: AppStart`
   - `Trace finished: AppStart, duration: NNN ms`
   - `Event created: PerformanceEvent(eventId=..., eventType=CUSTOM_TRACE, ...)`
4. Call `PerfSDK.getPendingEvents()` from `MainActivity` after `stopTrace` and log the list size — should be 1.
5. Start the same trace twice — second `startTrace` should log a warning, not crash.
6. Call `stopTrace` without `startTrace` — should log an error, not crash.
