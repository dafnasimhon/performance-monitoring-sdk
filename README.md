# PerfSDK - Android Performance Monitoring SDK

A full-stack Android performance monitoring solution

## Overview

PerfSDK automatically collects performance data from Android apps - startup time, screen load time, network latency, and custom traces - batches it locally, and ships it to a backend for aggregation and display on a real-time React dashboard.

---
## Demo
[![PerfSDK Demo](https://img.youtube.com/vi/gjr-lILGdEw/0.jpg)](https://youtu.be/gjr-llLGdEw)

--
## Architecture

```
Android App (QuickShop demo)
        │
        ▼
PerfSDK (Kotlin library module)
        │  auto-tracks: APP_STARTUP, SCREEN_LOAD, NETWORK_REQUEST, CUSTOM_TRACE
        │  stores events in Room DB → batches every 30s or 20 events
        ▼
FastAPI Backend  →  MongoDB Atlas
        ▲
        │  aggregation pipelines (avg, P95, error rate, sessions)
        ▼
React Dashboard (Vite + Recharts)
```

---

## Project Structure

```
seminar/
├── perfsdk/          ← SDK library module (Kotlin)
├── app/              ← QuickShop demo app
├── backend/          ← FastAPI + MongoDB backend
└── dashboard/        ← React dashboard (Vite + Recharts)
```

---

## SDK Features

| Feature | How it works |
|---|---|
| App Startup Time | Measured from `Application.onCreate()` to first `Activity.onResume()` |
| Screen Load Time | Auto-measured via `ActivityLifecycleCallbacks` - no code required |
| Network Tracking | OkHttp interceptor - add one line to your `OkHttpClient` |
| Custom Traces | `PerfSDK.startTrace("name")` / `PerfSDK.stopTrace("name")` |
| Batch Upload | Room DB → WorkManager + 30s timer → FastAPI endpoint |
| PII Protection | `DataSanitizer` strips sensitive fields before upload |
| Offline Support | Events persist in Room, retried automatically when network returns |
| Queue Limit | Max 1000 events in Room - oldest dropped when cap is reached |

### SDK Integration (3 steps)

**1. Initialize in your Application class:**
```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PerfSDK.init(this, "your-api-key")
    }
}
```

**2. Add the OkHttp interceptor (automatic network tracking):**
```kotlin
val okHttp = OkHttpClient.Builder()
    .addInterceptor(PerfSDK.okHttpInterceptor())
    .build()
```

**3. (Optional) Add custom traces:**
```kotlin
PerfSDK.startTrace("loadDashboard")
// ... your code ...
PerfSDK.stopTrace("loadDashboard")
```

That's it. Screen loads and app startup are tracked automatically with zero additional code.

---

## Backend Setup

### Requirements
- Python 3.10+
- MongoDB Atlas account (free tier works)

### Steps

```bash
cd backend
python -m venv venv
.\venv\Scripts\activate          # Windows
pip install -r requirements.txt
```

Create a `.env` file (never commit this):
```
MONGODB_URI=mongodb+srv://your-connection-string
```

Seed the API key and create indexes:
```bash
python seed_api_key.py
python create_indexes.py
```

Start the server:
```bash
uvicorn main:app --reload
```

API runs at `http://127.0.0.1:8000`. Swagger docs at `http://127.0.0.1:8000/docs`.

### API Endpoints

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/events/batch` | Receive events from the SDK |
| GET | `/api/v1/metrics/summary` | KPI totals + session count |
| GET | `/api/v1/metrics/startup` | App startup trend over time |
| GET | `/api/v1/metrics/screens` | Screen load stats (avg, P95, count) |
| GET | `/api/v1/metrics/network` | Network endpoint stats |
| GET | `/api/v1/metrics/traces` | Custom trace stats |
| GET | `/api/v1/metrics/health` | App health score (0–100) |
| GET | `/api/v1/metrics/network/errors` | Individual failed network calls |
| GET | `/api/v1/metrics/devices` | Performance grouped by device model |
| GET | `/api/v1/metrics/versions` | Performance grouped by app version |
| GET | `/api/v1/metrics/network-by-type` | WIFI vs CELLULAR comparison |
| GET | `/api/v1/metrics/events-over-time` | Event count grouped by hour |

All endpoints require `X-API-Key` header and support `from_ts`, `to_ts`, `app_version`, `network_type`, `device_model` query filters.

---

## Dashboard Setup

### Requirements
- Node.js 18+

```bash
cd dashboard
npm install
npm run dev
```

Dashboard runs at `http://localhost:5173`. Requires the backend to be running.

### Dashboard Sections

| Section | What it shows |
|---|---|
| App Health Score | 0–100 composite score (startup + screen + network + error rate) |
| KPI Cards | Sessions, avg startup, avg screen load, avg network, total events |
| Events Over Time | Bar chart of event volume grouped by hour |
| App Startup Trend | Line chart of startup durations over time |
| Slowest Devices | Device model table with avg + P95, color-coded |
| Performance by App Version | 3 separate bar charts (startup / screen / network) + P95 table |
| WIFI vs CELLULAR | Side-by-side network performance comparison |
| Slow Screens | Screen name, avg ms, P95, count |
| Slow Endpoints | Method, endpoint, avg ms, P95, error rate, count |
| Custom Traces | Trace name, avg ms, P95, count |
| Network Error Log | Individual failed calls with timestamp, status code, device |

### Health Score Formula

| Component | 25 pts | 15 pts | 5 pts | 0 pts |
|---|---|---|---|---|
| App Startup | < 500ms | < 1000ms | < 2000ms | ≥ 2000ms |
| Screen Load | < 200ms | < 500ms | < 1000ms | ≥ 1000ms |
| Network Speed | < 300ms | < 700ms | < 1500ms | ≥ 1500ms |
| Error Rate | 0% | < 1% | < 5% | ≥ 5% |

Score = (earned points / max possible) × 100. Grade: A ≥ 90, B ≥ 75, C ≥ 60, D ≥ 40, F < 40.
<img width="634" height="618" alt="image" src="https://github.com/user-attachments/assets/437032b4-0393-42e5-bf90-b71a9e638300" />

<img width="637" height="602" alt="image" src="https://github.com/user-attachments/assets/f8a47617-916c-40ff-9533-f3e03dc27a66" />

<img width="674" height="685" alt="image" src="https://github.com/user-attachments/assets/d00552bc-266e-4d6d-a067-b12a290799ed" />

---

<<<<<<< HEAD
## Demo App - QuickShop
=======

## Demo App — QuickShop
>>>>>>> 288c2e0429d0b0c72df724e54bb0fdb2228ec4e2

A mini e-commerce app using [fakestoreapi.com](https://fakestoreapi.com) that exercises every SDK feature.

**Screens:** Splash → Home → Product List → Product Detail → Cart → Login

**What gets tracked in a single demo run:**
- 1 APP_STARTUP event (random 600–2400ms so the trend chart shows variation)
- 6 SCREEN_LOAD events (one per activity)
- 5 NETWORK_REQUEST events (auto-tracked by interceptor)
- 5 CUSTOM_TRACE events: `loadHomePage`, `renderProductGrid`, `calculateTotal`, `renderCart`, `validateForm`
- 1 network error: `POST /auth/login 401` (intentional wrong-password demo)

---

## Security

- API keys are stored as SHA-256 hashes in MongoDB - never in plain text
- The OkHttp interceptor strips query parameters from URLs before tracking (prevents PII leakage)
- `.env` is listed in `.gitignore` and must never be committed
- `DataSanitizer` removes sensitive fields from events before upload

---

## Tech Stack

| Layer | Technology |
|---|---|
| Android SDK | Kotlin, Room, WorkManager, Retrofit, OkHttp |
| Demo App | AppCompatActivity, ViewBinding, RecyclerView, Glide |
| Backend | Python, FastAPI, Motor (async MongoDB), Pydantic |
| Database | MongoDB Atlas |
| Dashboard | React, Vite, Recharts, Axios |
