from datetime import datetime, timezone, timedelta
from typing import Optional

from config.database import get_db


def _now_ms() -> int:
    return int(datetime.now(timezone.utc).timestamp() * 1000)


def _week_ago_ms() -> int:
    return int((datetime.now(timezone.utc) - timedelta(days=7)).timestamp() * 1000)


def _p95(values: list) -> float:
    if not values:
        return 0.0
    s = sorted(values)
    return float(s[min(int(len(s) * 0.95), len(s) - 1)])


def _base_match(
    app_id: str,
    event_type: str,
    from_ts: int,
    to_ts: int,
    app_version: Optional[str],
    network_type: Optional[str],
    device_model: Optional[str],
) -> dict:
    match: dict = {
        "appId": app_id,
        "eventType": event_type,
        "timestamp": {"$gte": from_ts, "$lte": to_ts},
    }
    if app_version:
        match["appVersion"] = app_version
    if network_type:
        match["networkType"] = network_type
    if device_model:
        match["deviceModel"] = device_model
    return match


async def get_summary(app_id, from_ts, to_ts, app_version, network_type, device_model):
    db = get_db()
    base: dict = {"appId": app_id, "timestamp": {"$gte": from_ts, "$lte": to_ts}}
    if app_version:
        base["appVersion"] = app_version
    if network_type:
        base["networkType"] = network_type
    if device_model:
        base["deviceModel"] = device_model

    breakdown: dict[str, int] = {}
    total = 0
    async for doc in db["raw_events"].aggregate([
        {"$match": base},
        {"$group": {"_id": "$eventType", "count": {"$sum": 1}}},
    ]):
        breakdown[doc["_id"]] = doc["count"]
        total += doc["count"]

    session_count = 0
    async for doc in db["raw_events"].aggregate([
        {"$match": base},
        {"$group": {"_id": "$sessionId"}},
        {"$count": "count"},
    ]):
        session_count = doc.get("count", 0)

    async def _avg(event_type: str) -> Optional[float]:
        async for doc in db["raw_events"].aggregate([
            {"$match": {**base, "eventType": event_type}},
            {"$group": {"_id": None, "avg": {"$avg": "$durationMs"}}},
        ]):
            v = doc.get("avg")
            return round(v, 1) if v is not None else None
        return None

    return {
        "totalEvents": total,
        "sessionCount": session_count,
        "avgStartupMs": await _avg("APP_STARTUP"),
        "avgScreenLoadMs": await _avg("SCREEN_LOAD"),
        "avgNetworkMs": await _avg("NETWORK_REQUEST"),
        "eventTypeBreakdown": breakdown,
    }


async def get_startup_trend(app_id, from_ts, to_ts, app_version, network_type, device_model):
    db = get_db()
    match = _base_match(app_id, "APP_STARTUP", from_ts, to_ts, app_version, network_type, device_model)
    cursor = db["raw_events"].find(
        match,
        {"_id": 0, "timestamp": 1, "durationMs": 1, "appVersion": 1, "networkType": 1},
    ).sort("timestamp", 1)
    return [doc async for doc in cursor]


async def get_screen_stats(app_id, from_ts, to_ts, app_version, network_type, device_model):
    db = get_db()
    match = _base_match(app_id, "SCREEN_LOAD", from_ts, to_ts, app_version, network_type, device_model)
    results = []
    async for doc in db["raw_events"].aggregate([
        {"$match": match},
        {"$group": {
            "_id": "$eventName",
            "avg": {"$avg": "$durationMs"},
            "count": {"$sum": 1},
            "durations": {"$push": "$durationMs"},
        }},
        {"$sort": {"avg": -1}},
    ]):
        results.append({
            "screenName": doc["_id"],
            "avgMs": round(doc["avg"], 1),
            "p95Ms": _p95(doc["durations"]),
            "count": doc["count"],
        })
    return results


async def get_network_stats(app_id, from_ts, to_ts, app_version, network_type, device_model):
    db = get_db()
    match = _base_match(app_id, "NETWORK_REQUEST", from_ts, to_ts, app_version, network_type, device_model)
    results = []
    async for doc in db["raw_events"].aggregate([
        {"$match": match},
        {"$group": {
            "_id": {"method": "$method", "endpoint": "$endpoint"},
            "avg": {"$avg": "$durationMs"},
            "count": {"$sum": 1},
            "durations": {"$push": "$durationMs"},
            "errors": {"$sum": {"$cond": [{"$eq": ["$success", False]}, 1, 0]}},
        }},
        {"$sort": {"avg": -1}},
    ]):
        count = doc["count"]
        results.append({
            "method": doc["_id"].get("method") or "",
            "endpoint": doc["_id"].get("endpoint") or "",
            "avgMs": round(doc["avg"], 1),
            "p95Ms": _p95(doc["durations"]),
            "count": count,
            "errorRate": round(doc["errors"] / count, 3) if count else 0,
        })
    return results


async def get_slowest_devices(app_id, from_ts, to_ts, app_version, network_type, device_model):
    db = get_db()
    base: dict = {"appId": app_id, "timestamp": {"$gte": from_ts, "$lte": to_ts}}
    if app_version:  base["appVersion"]  = app_version
    if network_type: base["networkType"] = network_type
    if device_model: base["deviceModel"] = device_model

    pipeline = [
        {"$match": base},
        {"$group": {
            "_id": {"deviceModel": "$deviceModel", "eventType": "$eventType"},
            "avg": {"$avg": "$durationMs"},
            "count": {"$sum": 1},
            "durations": {"$push": "$durationMs"},
        }},
    ]

    raw: dict[str, dict] = {}
    async for doc in db["raw_events"].aggregate(pipeline):
        dev = doc["_id"]["deviceModel"] or "Unknown"
        et  = doc["_id"]["eventType"]
        if dev not in raw:
            raw[dev] = {
                "deviceModel": dev,
                "avgStartupMs": None, "p95StartupMs": None,
                "avgScreenMs":  None, "p95ScreenMs":  None,
                "avgNetworkMs": None, "p95NetworkMs":  None,
                "count": 0,
            }
        if et == "APP_STARTUP":
            raw[dev]["avgStartupMs"] = round(doc["avg"], 1)
            raw[dev]["p95StartupMs"] = _p95(doc["durations"])
        elif et == "SCREEN_LOAD":
            raw[dev]["avgScreenMs"]  = round(doc["avg"], 1)
            raw[dev]["p95ScreenMs"]  = _p95(doc["durations"])
        elif et == "NETWORK_REQUEST":
            raw[dev]["avgNetworkMs"] = round(doc["avg"], 1)
            raw[dev]["p95NetworkMs"] = _p95(doc["durations"])
        raw[dev]["count"] += doc["count"]

    return sorted(raw.values(), key=lambda d: d["avgStartupMs"] or 0, reverse=True)


async def get_version_stats(app_id, from_ts, to_ts, network_type, device_model):
    db = get_db()
    base: dict = {"appId": app_id, "timestamp": {"$gte": from_ts, "$lte": to_ts}}
    if network_type: base["networkType"] = network_type
    if device_model: base["deviceModel"] = device_model

    pipeline = [
        {"$match": base},
        {"$group": {
            "_id": {"appVersion": "$appVersion", "eventType": "$eventType"},
            "avg": {"$avg": "$durationMs"},
            "count": {"$sum": 1},
            "durations": {"$push": "$durationMs"},
            "errors": {"$sum": {"$cond": [{"$eq": ["$success", False]}, 1, 0]}},
        }},
    ]

    raw: dict[str, dict] = {}
    async for doc in db["raw_events"].aggregate(pipeline):
        ver = doc["_id"]["appVersion"] or "Unknown"
        et  = doc["_id"]["eventType"]
        if ver not in raw:
            raw[ver] = {"appVersion": ver,
                        "avgStartupMs": None, "p95StartupMs": None,
                        "avgScreenMs":  None, "p95ScreenMs":  None,
                        "avgNetworkMs": None, "p95NetworkMs": None,
                        "errorRate": 0.0, "count": 0,
                        "_netTotal": 0, "_netErrors": 0}
        if et == "APP_STARTUP":
            raw[ver]["avgStartupMs"] = round(doc["avg"], 1)
            raw[ver]["p95StartupMs"] = _p95(doc["durations"])
        elif et == "SCREEN_LOAD":
            raw[ver]["avgScreenMs"]  = round(doc["avg"], 1)
            raw[ver]["p95ScreenMs"]  = _p95(doc["durations"])
        elif et == "NETWORK_REQUEST":
            raw[ver]["avgNetworkMs"] = round(doc["avg"], 1)
            raw[ver]["p95NetworkMs"] = _p95(doc["durations"])
            raw[ver]["_netTotal"]   += doc["count"]
            raw[ver]["_netErrors"]  += doc["errors"]
        raw[ver]["count"] += doc["count"]

    results = []
    for v in raw.values():
        total = v.pop("_netTotal")
        errs  = v.pop("_netErrors")
        v["errorRate"] = round(errs / total * 100, 2) if total else 0.0
        results.append(v)

    return sorted(results, key=lambda v: v["appVersion"] or "", reverse=True)



async def get_network_by_type(app_id, from_ts, to_ts, app_version, device_model):
    db = get_db()
    match: dict = {"appId": app_id, "eventType": "NETWORK_REQUEST",
                   "timestamp": {"$gte": from_ts, "$lte": to_ts}}
    if app_version:  match["appVersion"]  = app_version
    if device_model: match["deviceModel"] = device_model

    results = []
    async for doc in db["raw_events"].aggregate([
        {"$match": match},
        {"$group": {
            "_id": "$networkType",
            "avg": {"$avg": "$durationMs"},
            "count": {"$sum": 1},
            "durations": {"$push": "$durationMs"},
            "errors": {"$sum": {"$cond": [{"$eq": ["$success", False]}, 1, 0]}},
        }},
        {"$sort": {"avg": 1}},
    ]):
        count = doc["count"]
        results.append({
            "networkType": doc["_id"] or "UNKNOWN",
            "avgMs":       round(doc["avg"], 1),
            "p95Ms":       _p95(doc["durations"]),
            "errorRate":   round(doc["errors"] / count * 100, 2) if count else 0.0,
            "count":       count,
        })
    return results


async def get_health_score(app_id, from_ts, to_ts, app_version, network_type, device_model):
    db = get_db()
    base: dict = {"appId": app_id, "timestamp": {"$gte": from_ts, "$lte": to_ts}}
    if app_version:  base["appVersion"]  = app_version
    if network_type: base["networkType"] = network_type
    if device_model: base["deviceModel"] = device_model

    async def _avg(event_type: str) -> Optional[float]:
        async for doc in db["raw_events"].aggregate([
            {"$match": {**base, "eventType": event_type}},
            {"$group": {"_id": None, "avg": {"$avg": "$durationMs"}}},
        ]):
            return doc.get("avg")
        return None

    async def _error_rate() -> float:
        total = errors = 0
        async for doc in db["raw_events"].aggregate([
            {"$match": {**base, "eventType": "NETWORK_REQUEST"}},
            {"$group": {
                "_id": None,
                "total": {"$sum": 1},
                "errors": {"$sum": {"$cond": [{"$eq": ["$success", False]}, 1, 0]}},
            }},
        ]):
            total  = doc.get("total", 0)
            errors = doc.get("errors", 0)
        return (errors / total) if total else 0.0

    avg_startup = await _avg("APP_STARTUP")
    avg_screen  = await _avg("SCREEN_LOAD")
    avg_network = await _avg("NETWORK_REQUEST")
    error_rate  = await _error_rate()

    def _startup_pts(ms):
        if ms is None: return None
        if ms < 500:   return 25
        if ms < 1000:  return 15
        if ms < 2000:  return 5
        return 0

    def _screen_pts(ms):
        if ms is None: return None
        if ms < 200:   return 25
        if ms < 500:   return 15
        if ms < 1000:  return 5
        return 0

    def _network_pts(ms):
        if ms is None: return None
        if ms < 300:   return 25
        if ms < 700:   return 15
        if ms < 1500:  return 5
        return 0

    def _error_pts(rate):
        if rate == 0:    return 25
        if rate < 0.01:  return 20
        if rate < 0.05:  return 10
        return 0

    components = {
        "startup":  _startup_pts(avg_startup),
        "screen":   _screen_pts(avg_screen),
        "network":  _network_pts(avg_network),
        "errorRate": _error_pts(error_rate),
    }

    scored = [v for v in components.values() if v is not None]
    if not scored:
        return {"score": None, "grade": "N/A", "components": components, "details": {
            "avgStartupMs": avg_startup, "avgScreenMs": avg_screen,
            "avgNetworkMs": avg_network, "errorRate": error_rate,
        }}

    # Scale to 100 based on how many components have data
    score = round(sum(scored) / (len(scored) * 25) * 100)

    if score >= 90:   grade = "A"
    elif score >= 75: grade = "B"
    elif score >= 60: grade = "C"
    elif score >= 40: grade = "D"
    else:             grade = "F"

    return {
        "score": score,
        "grade": grade,
        "components": components,
        "details": {
            "avgStartupMs": round(avg_startup, 1) if avg_startup else None,
            "avgScreenMs":  round(avg_screen, 1)  if avg_screen  else None,
            "avgNetworkMs": round(avg_network, 1) if avg_network else None,
            "errorRate":    round(error_rate * 100, 2),
        },
    }


async def get_network_errors(app_id, from_ts, to_ts, app_version, network_type, device_model, limit=100):
    db = get_db()
    match = _base_match(app_id, "NETWORK_REQUEST", from_ts, to_ts, app_version, network_type, device_model)
    match["success"] = False
    results = []
    cursor = db["raw_events"].find(
        match,
        {
            "_id": 0,
            "timestamp": 1,
            "method": 1,
            "endpoint": 1,
            "statusCode": 1,
            "durationMs": 1,
            "deviceModel": 1,
            "networkType": 1,
            "appVersion": 1,
        },
    ).sort("timestamp", -1).limit(limit)
    async for doc in cursor:
        results.append(doc)
    return results


async def get_events_over_time(app_id, from_ts, to_ts, app_version, network_type, device_model):
    db = get_db()
    base: dict = {"appId": app_id, "timestamp": {"$gte": from_ts, "$lte": to_ts}}
    if app_version:  base["appVersion"]  = app_version
    if network_type: base["networkType"] = network_type
    if device_model: base["deviceModel"] = device_model

    HOUR_MS = 3_600_000
    pipeline = [
        {"$match": base},
        {"$group": {
            "_id": {"$subtract": ["$timestamp", {"$mod": ["$timestamp", HOUR_MS]}]},
            "count": {"$sum": 1},
        }},
        {"$sort": {"_id": 1}},
    ]
    results = []
    async for doc in db["raw_events"].aggregate(pipeline):
        results.append({"hourTs": doc["_id"], "count": doc["count"]})
    return results


async def get_trace_stats(app_id, from_ts, to_ts, app_version, network_type, device_model):
    db = get_db()
    match = _base_match(app_id, "CUSTOM_TRACE", from_ts, to_ts, app_version, network_type, device_model)
    results = []
    async for doc in db["raw_events"].aggregate([
        {"$match": match},
        {"$group": {
            "_id": "$eventName",
            "avg": {"$avg": "$durationMs"},
            "count": {"$sum": 1},
            "durations": {"$push": "$durationMs"},
        }},
        {"$sort": {"avg": -1}},
    ]):
        results.append({
            "traceName": doc["_id"],
            "avgMs": round(doc["avg"], 1),
            "p95Ms": _p95(doc["durations"]),
            "count": doc["count"],
        })
    return results
