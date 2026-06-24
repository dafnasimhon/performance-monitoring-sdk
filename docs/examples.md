# Examples

Real code examples from the **QuickShop** demo app.

---

## Demo App Overview

QuickShop is a mini e-commerce app built with fakestoreapi.com that exercises every SDK feature. It is included in this repo under `app/`.

::: info Watch the demo
<!-- Replace with your YouTube embed URL after recording -->
<!-- <iframe width="100%" height="400" src="https://www.youtube.com/embed/YOUR_VIDEO_ID" frameborder="0" allowfullscreen></iframe> -->
Add a YouTube embed here after recording your demo video.
:::

---

## Example 1 — SDK Initialization

```kotlin
// app/src/main/java/com/example/mysdk/MyApplication.kt
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PerfSDK.init(this, "dev-api-key")
    }
}
```

**What it tracks automatically from this point:**
- App startup time (measured to first `onResume`)
- Screen load time for every Activity

---

## Example 2 — OkHttp Interceptor

```kotlin
// app/src/main/java/com/example/mysdk/api/RetrofitClient.kt
object RetrofitClient {

    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(PerfSDK.okHttpInterceptor())  // ← one line
        .build()

    val api: FakeStoreApi = Retrofit.Builder()
        .baseUrl("https://fakestoreapi.com/")
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(FakeStoreApi::class.java)
}
```

**Logcat output when HomeActivity loads:**
```
D/PerfSDK  Network call tracked: GET /products/categories 200 (312ms)
D/PerfSDK  Network call tracked: GET /products 200 (287ms)
```

---

## Example 3 — Custom Traces

```kotlin
// HomeActivity.kt
lifecycleScope.launch {
    PerfSDK.startTrace("loadHomePage")

    val categories = async { api.getCategories() }
    val products   = async { api.getProducts(limit = 6) }

    categories.await()
    products.await()

    PerfSDK.stopTrace("loadHomePage")

    PerfSDK.startTrace("renderProductGrid")
    // ... set up RecyclerView adapter ...
    PerfSDK.stopTrace("renderProductGrid")
}
```

**Logcat output:**
```
D/PerfSDK  Trace started: loadHomePage
D/PerfSDK  Trace started: renderProductGrid
D/PerfSDK  Trace finished: loadHomePage, duration: 1204ms
D/PerfSDK  Trace finished: renderProductGrid, duration: 153ms
```

---

## Example 4 — Traces with Variable Duration

Making the delay depend on data so P95 differs from the average:

```kotlin
// CartActivity.kt
PerfSDK.startTrace("calculateTotal")
val total = withContext(Dispatchers.Default) {
    var sum = 0.0
    for (product in items) {
        sum += product.price
        Thread.sleep((product.price % 40 + 8).toLong()) // varies by price
    }
    sum
}
PerfSDK.stopTrace("calculateTotal")
```

This produces meaningful P95 data in the dashboard — a $109 backpack takes ~33ms, a $15 shirt takes ~23ms, so carts with expensive items show noticeably higher P95.

---

## Example 5 — Intentional Error for Demo

```kotlin
// LoginActivity.kt — two buttons:

// Button 1: valid credentials → 200
binding.btnLogin.setOnClickListener {
    performLogin(username = "johnd", password = "m38rmF$")
}

// Button 2: wrong password → 401, appears in Network Error Log
binding.btnWrongLogin.setOnClickListener {
    performLogin(username = "johnd", password = "wrongpassword")
}
```

The 401 response is automatically captured by the interceptor and appears in the **Network Error Log** section of the dashboard with the timestamp, device model, and network type.

---

## What the Dashboard Shows After a Full Run

| Dashboard section | Expected data |
|---|---|
| Sessions | 1 per app launch |
| App Startup Trend | Varied 600–2400ms (random splash delay) |
| Slow Screens | 6 rows (one per Activity) |
| Slow Endpoints | 5 rows (auto-tracked by interceptor) |
| Custom Traces | 5 rows: `loadHomePage`, `renderProductGrid`, `calculateTotal`, `renderCart`, `validateForm` |
| Network Error Log | 1 row: `POST /auth/login 401` |
| Performance by Version | Bars for 1.0 and 2.0 side by side |

---

## Screenshots

![Dashboard — top section](../public/images/Screenshot_dashboard1.png)

![Dashboard — version comparison and slow screens](../public/images/Screenshot_dashboard2.png)

![Dashboard — endpoints, traces, error log](../public/images/Screenshot_dashboard3.png)

::: info Adding more screenshots
To add individual section screenshots, save them to `docs/public/images/` and reference them with:

| Section | Filename |
|---|---|
| Full dashboard | `dashboard-overview.png` |
| Health score card | `dashboard-health-score.png` |
| KPI cards row | `dashboard-kpi-cards.png` |
| Startup trend chart | `dashboard-startup-trend.png` |
| Events over time chart | `dashboard-events-over-time.png` |
| Version comparison charts | `dashboard-version-stats.png` |
| Slow screens table | `dashboard-slow-screens.png` |
| Custom traces table | `dashboard-traces.png` |
| Network error log | `dashboard-error-log.png` |

Then replace each placeholder with:
```md
![Dashboard overview](../public/images/dashboard-overview.png)
```
:::
