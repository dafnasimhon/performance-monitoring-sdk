# API Reference

## PerfSDK (Public API)

### `init(context, apiKey)`

Initializes the SDK. Must be called in `Application.onCreate()`.

```kotlin
PerfSDK.init(context: Context, apiKey: String)
```

| Parameter | Type | Description |
|---|---|---|
| `context` | `Context` | Application context |
| `apiKey` | `String` | Your API key (stored as SHA-256 on the server) |

Calling `init()` twice is safe — the second call logs a warning and returns immediately.

---

### `okHttpInterceptor()`

Returns an `Interceptor` that automatically tracks all OkHttp network calls.

```kotlin
PerfSDK.okHttpInterceptor(): Interceptor
```

Add to your `OkHttpClient.Builder()`. Tracks method, path (no query params), status code, and latency.

---

### `startTrace(traceName)`

Starts a named custom trace. Records the current timestamp.

```kotlin
PerfSDK.startTrace(traceName: String)
```

| Parameter | Type | Description |
|---|---|---|
| `traceName` | `String` | Unique name for this trace. Must not be blank. |

::: warning
Starting the same trace name twice logs a warning. Always pair with `stopTrace()`.
:::

---

### `stopTrace(traceName)`

Stops a running trace and records the `CUSTOM_TRACE` event.

```kotlin
PerfSDK.stopTrace(traceName: String)
```

| Parameter | Type | Description |
|---|---|---|
| `traceName` | `String` | Must match the name passed to `startTrace()`. |

Calling `stopTrace()` without a matching `startTrace()` logs an error and does nothing.

---

### `trackNetworkCall(method, endpoint, statusCode, latencyMs)`

Manually records a network call. Use this only if you are **not** using the OkHttp interceptor.

```kotlin
PerfSDK.trackNetworkCall(
    method: String,
    endpoint: String,
    statusCode: Int,
    latencyMs: Long
)
```

| Parameter | Type | Description |
|---|---|---|
| `method` | `String` | HTTP method: `"GET"`, `"POST"`, etc. |
| `endpoint` | `String` | URL path (no query params — strip them manually) |
| `statusCode` | `Int` | HTTP status code. Use `0` for network errors. |
| `latencyMs` | `Long` | Round-trip time in milliseconds |

---

### `flush()`

Forces an immediate upload of all pending events.

```kotlin
PerfSDK.flush()
```

Runs both an in-process coroutine and a `OneTimeWorkRequest` for reliability.

---

### `sessionId`

The current session UUID. Generated once per process start, attached to every event.

```kotlin
val id: String = PerfSDK.sessionId
```

---

## Backend REST API

All endpoints require:
- Header: `X-API-Key: your-api-key`
- Base URL: `http://your-server:8000/api/v1`

Common query parameters (all optional):

| Parameter | Type | Description |
|---|---|---|
| `from_ts` | `int` | Start time (epoch ms). Default: 7 days ago |
| `to_ts` | `int` | End time (epoch ms). Default: now |
| `app_version` | `string` | Filter by version (e.g. `"2.0"`) |
| `network_type` | `string` | Filter by `"WIFI"` or `"CELLULAR"` |
| `device_model` | `string` | Filter by device model name |

---

### POST `/events/batch`

Receives a batch of events from the SDK.

**Request body:**
```json
{
  "sentAt": 1781340000000,
  "events": [
    {
      "eventId": "uuid",
      "sessionId": "uuid",
      "eventType": "SCREEN_LOAD",
      "eventName": "HomeActivity",
      "durationMs": 342,
      "timestamp": 1781340000000,
      "appVersion": "2.0",
      "deviceModel": "Pixel 8",
      "networkType": "WIFI"
    }
  ]
}
```

**Response:**
```json
{ "status": "success", "accepted": 20 }
```

---

### GET `/metrics/summary`

Returns overall KPIs for the selected time range.

```json
{
  "totalEvents": 240,
  "sessionCount": 9,
  "avgStartupMs": 1042.5,
  "avgScreenLoadMs": 159.3,
  "avgNetworkMs": 527.1,
  "eventTypeBreakdown": {
    "APP_STARTUP": 9,
    "SCREEN_LOAD": 54,
    "NETWORK_REQUEST": 135,
    "CUSTOM_TRACE": 42
  }
}
```

---

### GET `/metrics/health`

Returns the App Health Score.

```json
{
  "score": 72,
  "grade": "B",
  "components": {
    "startup": 15,
    "screen": 25,
    "network": 15,
    "errorRate": 20
  },
  "details": {
    "avgStartupMs": 1042.5,
    "avgScreenMs": 159.3,
    "avgNetworkMs": 527.1,
    "errorRate": 3.7
  }
}
```

**Scoring thresholds:**

| Component | 25 pts | 15 pts | 5 pts | 0 pts |
|---|---|---|---|---|
| App Startup | < 500ms | < 1000ms | < 2000ms | ≥ 2000ms |
| Screen Load | < 200ms | < 500ms | < 1000ms | ≥ 1000ms |
| Network Speed | < 300ms | < 700ms | < 1500ms | ≥ 1500ms |
| Error Rate | 0% | < 1% | < 5% | ≥ 5% |

---

### GET `/metrics/screens`

Returns screen load stats grouped by activity name, sorted slowest first.

```json
[
  { "screenName": "ProductDetailActivity", "avgMs": 312.4, "p95Ms": 891.0, "count": 18 },
  { "screenName": "HomeActivity",          "avgMs": 159.3, "p95Ms": 707.0, "count": 9 }
]
```

---

### GET `/metrics/network`

Returns network stats grouped by `method + endpoint`.

```json
[
  { "method": "GET", "endpoint": "/products", "avgMs": 527.1, "p95Ms": 1280.0, "errorRate": 0.037, "count": 45 }
]
```

---

### GET `/metrics/network/errors`

Returns individual failed network calls (`success = false`), newest first.

```json
[
  {
    "timestamp": 1781340000000,
    "method": "POST",
    "endpoint": "/auth/login",
    "statusCode": 401,
    "durationMs": 211,
    "deviceModel": "sdk_gphone64_x86_64",
    "networkType": "WIFI",
    "appVersion": "2.0"
  }
]
```
