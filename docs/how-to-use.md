# How to Use

## Initialization

Call `PerfSDK.init()` once in your `Application.onCreate()`. It must be called before any Activity starts.

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PerfSDK.init(this, "your-api-key")
    }
}
```

Calling `init()` a second time is safe — it logs a warning and does nothing.

---

## Automatic Tracking

Once initialized, the following are tracked with **zero additional code**:

### App Startup Time
Measured from `PerfSDK.init()` to the first `Activity.onResume()`. Shows up as `APP_STARTUP` in the dashboard.

### Screen Load Time
Measured from `Activity.onCreate()` to `Activity.onResume()` for every activity. Shows up as `SCREEN_LOAD` with the activity class name.

---

## Network Tracking

Add the interceptor to your `OkHttpClient`:

```kotlin
val okHttp = OkHttpClient.Builder()
    .addInterceptor(PerfSDK.okHttpInterceptor())
    .build()

val retrofit = Retrofit.Builder()
    .baseUrl("https://api.example.com/")
    .client(okHttp)
    .build()
```

Every call through this client is tracked automatically. The interceptor records:
- HTTP method (`GET`, `POST`, etc.)
- URL path (query params stripped)
- Status code
- Latency in ms
- Success / failure

::: warning
Do not also call `PerfSDK.trackNetworkCall()` manually for the same calls — that would double-count them.
:::

---

## Custom Traces

Wrap any block of code you want to measure with `startTrace` / `stopTrace`:

```kotlin
PerfSDK.startTrace("loadDashboard")

// ... your code ...

PerfSDK.stopTrace("loadDashboard")
```

Rules:
- Trace names must be unique per active trace — starting the same name twice logs a warning
- Calling `stopTrace` without `startTrace` logs an error and does nothing
- Traces appear in the **Custom Traces** table on the dashboard

---

## Manual Network Tracking

If you are **not** using OkHttp (e.g., `HttpURLConnection`, `Ktor`), you can call `trackNetworkCall()` manually:

```kotlin
val start = System.currentTimeMillis()
val response = myHttpClient.get("/products")
PerfSDK.trackNetworkCall(
    method     = "GET",
    endpoint   = "/products",
    statusCode = response.code,
    latencyMs  = System.currentTimeMillis() - start
)
```

---

## Flushing Events

Events are sent automatically every 30 seconds. To force an immediate upload (e.g., before the user closes the app):

```kotlin
PerfSDK.flush()
```

`flush()` runs both an in-process coroutine (fast) and a `OneTimeWorkRequest` (reliable).

---

## App Version

The SDK reads `versionName` from your `PackageInfo` automatically during `init()` and attaches it to every event. To compare versions in the dashboard, increment `versionName` in `app/build.gradle.kts`:

```kotlin
defaultConfig {
    versionCode = 2
    versionName = "2.0"
}
```

---

## Checking Events in Real Time

Filter Logcat by tag `PerfSDK` to see every event as it is recorded and uploaded:

```
D/PerfSDK  PerfSDK initialized
D/PerfSDK  Lifecycle observer registered
D/PerfSDK  Trace started: loadHomePage
D/PerfSDK  Network call tracked: GET /products 200 (287ms)
D/PerfSDK  Trace finished: loadHomePage, duration: 1053ms
D/PerfSDK  Batch sent: 12 events accepted
```

To inspect the local Room database in real time: **Android Studio → App Inspection → Database Inspector → events table**.
