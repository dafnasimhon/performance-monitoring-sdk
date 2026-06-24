# How It Works

## Architecture

```
Android Application
        │
        ▼
┌─────────────────────────────────────┐
│  PerfSDK  (Kotlin library module)   │
│                                     │
│  ┌─────────────────────────────┐    │
│  │  PerfLifecycleObserver      │    │ ← auto: APP_STARTUP, SCREEN_LOAD
│  │  PerfInterceptor (OkHttp)   │    │ ← auto: NETWORK_REQUEST
│  │  startTrace / stopTrace     │    │ ← manual: CUSTOM_TRACE
│  └──────────────┬──────────────┘    │
│                 │                   │
│  ┌──────────────▼──────────────┐    │
│  │  Room Database (events)     │    │ ← local persistence
│  └──────────────┬──────────────┘    │
│                 │                   │
│  ┌──────────────▼──────────────┐    │
│  │  BatchSender + WorkManager  │    │ ← upload: 30s timer + background
│  └──────────────┬──────────────┘    │
└─────────────────┼───────────────────┘
                  │  POST /api/v1/events/batch
                  ▼
        FastAPI Backend
                  │
                  ▼
           MongoDB Atlas
                  ▲
                  │  GET /api/v1/metrics/*
                  ▼
        React Dashboard
```

---

## Event Types

PerfSDK collects four types of performance events:

| Event Type | Triggered by | Key fields |
|---|---|---|
| `APP_STARTUP` | App process start → first Activity `onResume` | `durationMs` |
| `SCREEN_LOAD` | Activity `onCreate` → `onResume` | `eventName` (screen name), `durationMs` |
| `NETWORK_REQUEST` | OkHttp interceptor | `method`, `endpoint`, `statusCode`, `durationMs`, `success` |
| `CUSTOM_TRACE` | `startTrace()` / `stopTrace()` | `eventName`, `durationMs` |

Every event also carries device context:

| Field | Example |
|---|---|
| `sessionId` | UUID — groups all events from one app launch |
| `appVersion` | `"2.0"` |
| `deviceModel` | `"Pixel 8"` |
| `androidVersion` | `"14"` |
| `networkType` | `"WIFI"` / `"CELLULAR"` |
| `availableRamMb` | `3840` |
| `isBatterySaverActive` | `false` |
| `isEmulator` | `false` |

---

## Data Flow

### Step 1 — Measurement

The SDK's `PerfLifecycleObserver` registers as an `ActivityLifecycleCallback` during `PerfSDK.init()`. It fires automatically on every Activity start:

- `onCreate` → records the start timestamp
- `onResume` → records the end timestamp → emits `SCREEN_LOAD` event

App startup is measured by recording `init()` time and stopping at the first `onResume`.

### Step 2 — Local Storage

Every event is immediately written to **Room Database** on a background IO coroutine. This means:
- Events survive app kills and process restarts
- No events are lost during network outages
- The queue is capped at **1000 events** — oldest are dropped if the limit is reached

Upload statuses: `PENDING` → `SENDING` → deleted on success, or `FAILED` → reset to `PENDING` on next retry.

### Step 3 — Batched Upload

Two mechanisms send events to the backend:

| Mechanism | When | Trigger |
|---|---|---|
| In-process coroutine | App in foreground | Every 30 seconds, or when queue reaches 20 events |
| WorkManager periodic job | App backgrounded or killed | Every 15 minutes, with exponential backoff on failure |

`BatchSender` picks up to 20 `PENDING` events, marks them `SENDING`, POSTs them, then deletes on success. If the POST fails, events are marked `FAILED` and reset to `PENDING` on the next run.

### Step 4 — Aggregation

The backend runs MongoDB aggregation pipelines on demand — no pre-aggregated collections. Pipelines group events by screen, endpoint, device, version, or hour bucket and compute average, P95, count, and error rate.

**P95 calculation:**
```python
def _p95(values: list) -> float:
    s = sorted(values)
    return float(s[min(int(len(s) * 0.95), len(s) - 1)])
```

---

## Privacy & Security

- **Query parameters stripped** — the OkHttp interceptor uses `request.url.encodedPath` (no query string), so `/search?q=john@email.com` becomes `/search`
- **API keys hashed** — stored as SHA-256 in MongoDB, never in plain text
- **No PII collected** — no user IDs, names, emails, or request bodies
- **`.env` excluded** from git via `.gitignore`
