# Who Is PerfSDK For?

## Target Audience

PerfSDK is built for **Android development teams** that want to understand how their app performs in real users' hands — not just in the lab.

---

### Android Developers

You ship code and want to know whether a recent change made screens slower or network calls faster. PerfSDK gives you P95 latency per screen and per endpoint, broken down by app version — so you can see regressions the moment they appear in production.

### Tech Leads & Engineering Managers

You want a single health score for the app so you can track quality over time without reading raw logs. The dashboard's **App Health Score** (0–100, graded A–F) gives you an at-a-glance signal across four dimensions: startup, screen load, network speed, and error rate.

### QA Engineers

You want to reproduce performance issues reported by users. PerfSDK logs every slow screen and failed network call with device model, network type, and app version — so you can filter to exactly the conditions that trigger the issue.

### Product Managers

You want to know how many sessions happened today and whether users are hitting errors. The **Sessions KPI card** and **Network Error Log** give you that without touching any code.

---

## Use Cases

| Scenario | What PerfSDK tells you |
|---|---|
| Shipped a new feature — did it slow the app? | Compare P95 startup and screen load between version 1.0 and 2.0 |
| Users report the app feels slow on 4G | Filter dashboard by `network_type=CELLULAR` to see real latency |
| Login is failing for some users | Network Error Log shows exact status codes, devices, and timestamps |
| Want to optimize a specific screen | `Slow Screens` table ranks every screen by average and P95 load time |
| App is slow on old devices | `Slowest Devices` table groups performance by device model |

---

## What PerfSDK Is NOT

- Not a crash reporter (use Firebase Crashlytics for that)
- Not a user analytics tool (no user IDs, no session recordings)
- Not an APM for backend services (measures the Android client only)

It is a **focused, lightweight performance measurement SDK** — it does one thing and does it well.
