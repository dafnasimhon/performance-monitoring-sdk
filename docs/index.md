---
layout: home

hero:
  name: "PerfSDK"
  text: "Android Performance Monitoring"
  tagline: Automatically measure startup time, screen loads, network latency, and custom traces — with a single line of code.
  actions:
    - theme: brand
      text: Get Started
      link: /getting-started
    - theme: alt
      text: See the Dashboard
      link: /dashboard

features:
  - icon: ⚡
    title: Zero-effort tracking
    details: App startup and screen load times are measured automatically via lifecycle callbacks — no code changes required in your activities.

  - icon: 🌐
    title: Automatic network monitoring
    details: Add one OkHttp interceptor and every network call is tracked — method, endpoint, status code, and latency.

  - icon: 📊
    title: Real-time dashboard
    details: React dashboard with health scores, P95 latency, error logs, version comparisons, and WIFI vs CELLULAR breakdowns.

  - icon: 🔋
    title: Battery-friendly batching
    details: Events are stored in Room DB and uploaded in batches of 20 via WorkManager — no constant network activity.

  - icon: 🔒
    title: Privacy first
    details: Query parameters are stripped before tracking. API keys are stored as SHA-256 hashes. No PII ever leaves the device.

  - icon: 📱
    title: Works offline
    details: Events persist in Room DB when offline and are automatically retried when connectivity returns. Queue capped at 1000 events.
---
