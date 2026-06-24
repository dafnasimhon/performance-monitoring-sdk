# Dashboard

The PerfSDK dashboard is a React app built with Vite and Recharts. It visualises all collected performance data in real time.

## Setup

```bash
cd dashboard
npm install
npm run dev   # http://localhost:5173
```

Requires the FastAPI backend running at `http://127.0.0.1:8000`.

---

## Overview

![Dashboard top — Health Score, KPI cards, Events Over Time, Startup Trend, Slowest Devices](../public/images/Screenshot_dashboard1.png)

![Dashboard middle — Performance by App Version, WIFI vs CELLULAR, Slow Screens](../public/images/Screenshot_dashboard2.png)

![Dashboard bottom — Slow Endpoints, Custom Traces, Network Error Log](../public/images/Screenshot_dashboard3.png)

---

## Filters

At the top of the dashboard, four filters apply to **all sections simultaneously**:

| Filter | Options | Effect |
|---|---|---|
| Time range | 24h / 7d / 30d | Sets `from_ts` and `to_ts` on every API call |
| App Version | dropdown | Shows data for one specific version only |
| Network Type | WIFI / CELLULAR | Filters all events by connection type |
| Device Model | text input | Filters all events to one device |

---

## Sections

### App Health Score

A 0–100 composite score across four dimensions. Displayed as a large circle with a letter grade (A–F) and four progress bars.

| Grade | Score |
|---|---|
| A | ≥ 90 |
| B | ≥ 75 |
| C | ≥ 60 |
| D | ≥ 40 |
| F | < 40 |

Each component is worth up to 25 points. If a component has no data yet, it is excluded from the calculation so you are not penalised for missing data.

---

### KPI Cards

Five cards showing key numbers at a glance:

| Card | Source |
|---|---|
| Sessions | Distinct `sessionId` count — equals number of app launches |
| Avg App Startup | Average `durationMs` of all `APP_STARTUP` events |
| Avg Screen Load | Average `durationMs` of all `SCREEN_LOAD` events |
| Avg Network Req | Average `durationMs` of all `NETWORK_REQUEST` events |
| Total Events | Sum of all event types |

---

### Events Over Time

A bar chart grouping all events into 1-hour buckets. Shows when the app was being used most actively. Useful for spotting usage patterns and correlating performance issues with time of day.

---

### App Startup Trend

A line chart plotting every `APP_STARTUP` event over time. Each point is one app launch. Because the QuickShop demo uses a random 600–2400ms splash delay, the chart shows natural variation — making the P95 concept visible at a glance.

---

### Slowest Devices

A table grouping performance by device model. Columns: device name, avg startup, P95 startup, avg screen load, P95 screen load, avg network, P95 network, event count.

Color coding per cell:
- **Green** — below the "good" threshold
- **Yellow** — between good and warning
- **Orange** — between warning and bad
- **Red** — above the bad threshold

---

### Performance by App Version

Three separate bar charts (one per metric: startup / screen load / network latency) so each has its own Y axis. A shared Y axis would hide screen-load regressions (200ms) because startup bars (~1000ms) dominate the scale.

Below the charts: a full table with avg, P95, error rate (highlighted red if > 5%), and event count per version.

---

### WIFI vs CELLULAR

Two side-by-side cards comparing network performance by connection type. Each card shows avg ms, P95 ms, error rate, and event count. Useful for finding endpoints that are acceptable on WIFI but too slow on 4G.

---

### Slow Screens

A table of all `SCREEN_LOAD` events grouped by screen name, sorted slowest average first. Columns: screen name, avg ms, P95 ms, count.

---

### Slow Endpoints

A table of all `NETWORK_REQUEST` events grouped by `method + endpoint`, sorted slowest first. Error rate is highlighted red if above 1%.

---

### Custom Traces

A table of all `CUSTOM_TRACE` events grouped by trace name. Shows avg ms, P95 ms, and count. The gap between avg and P95 shows how much performance varies across runs.

---

### Network Error Log

A chronological list of individual failed network calls (`success = false`). Each row shows timestamp, method, endpoint, status code, latency, device model, network type, and app version. Useful for diagnosing specific failures.

---

::: info Adding screenshots
Take screenshots of each section and save them to `docs/public/images/`. Then add image tags to each section above:

```md
![Health score](../public/images/dashboard-health-score.png)
```

| Section | Filename |
|---|---|
| Full dashboard | `dashboard-overview.png` |
| Filters bar | `dashboard-filters.png` |
| Health score card | `dashboard-health-score.png` |
| KPI cards row | `dashboard-kpi-cards.png` |
| Events over time | `dashboard-events-over-time.png` |
| Startup trend | `dashboard-startup-trend.png` |
| Slowest devices | `dashboard-slowest-devices.png` |
| Version charts | `dashboard-version-stats.png` |
| WIFI vs CELLULAR | `dashboard-network-comparison.png` |
| Slow screens | `dashboard-slow-screens.png` |
| Slow endpoints | `dashboard-slow-endpoints.png` |
| Custom traces | `dashboard-traces.png` |
| Error log | `dashboard-error-log.png` |
:::
