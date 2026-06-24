# Get Started

Get PerfSDK running in your Android app in under 5 minutes.

## Prerequisites

- Android Studio Hedgehog or later
- `minSdk 26` or higher
- A running PerfSDK backend (see [backend setup](#backend-setup))

---

## 1. Add the SDK Module

PerfSDK is distributed as a local Gradle module. Copy the `perfsdk/` folder into your project root, then add it to `settings.gradle.kts`:

```kotlin
include(":app", ":perfsdk")
```

In your `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":perfsdk"))
}
```

Sync Gradle.

---

## 2. Initialize the SDK

Create an `Application` subclass (or use your existing one) and call `PerfSDK.init()`:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PerfSDK.init(this, "your-api-key")
    }
}
```

Register it in `AndroidManifest.xml`:

```xml
<application
    android:name=".MyApp"
    ...>
```

::: tip
`PerfSDK.init()` must be called in `Application.onCreate()` — not in any Activity — so the startup timer begins at process start.
:::

---

## 3. Add the Network Interceptor

Add one line to your `OkHttpClient` builder:

```kotlin
val okHttp = OkHttpClient.Builder()
    .addInterceptor(PerfSDK.okHttpInterceptor())
    .build()
```

That's it. Every HTTP call made through this client is now automatically tracked — method, endpoint, status code, and latency. No changes needed anywhere else.

---

## That's It

App startup and screen load times are measured **automatically** from this point forward. Navigate through your app and check Logcat filtered by `PerfSDK` to see events being tracked in real time:

```
D/PerfSDK: PerfSDK initialized
D/PerfSDK: Lifecycle observer registered
D/PerfSDK: Network call tracked: GET /products 200 (342ms)
D/PerfSDK: Trace finished: loadHomePage, duration: 1204ms
D/PerfSDK: Batch sent: 16 events accepted
```

---

## Backend Setup

The SDK sends events to a FastAPI backend backed by MongoDB Atlas.

```bash
cd backend
python -m venv venv
.\venv\Scripts\activate
pip install -r requirements.txt
```

Create `backend/.env`:

```
MONGODB_URI=mongodb+srv://your-connection-string
```

```bash
python seed_api_key.py    # creates the dev-api-key in MongoDB
python create_indexes.py  # creates query indexes
uvicorn main:app --reload # starts the server on port 8000
```

---

## Dashboard Setup

```bash
cd dashboard
npm install
npm run dev               # opens http://localhost:5173
```

The dashboard connects to the backend at `http://127.0.0.1:8000` automatically.

---

## Android Emulator Note

The emulator reaches your local machine at `10.0.2.2`, not `localhost`. The SDK's `RetrofitProvider` already uses `http://10.0.2.2:8000/` as the base URL. Make sure `network_security_config.xml` allows cleartext traffic to that address.
