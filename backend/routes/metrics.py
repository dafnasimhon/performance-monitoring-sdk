from datetime import datetime, timezone, timedelta
from typing import Optional

from fastapi import APIRouter, Depends, Header, HTTPException, Query

from services.event_service import validate_api_key
from services import metrics_service

router = APIRouter()


async def _get_app_id(x_api_key: str = Header(..., alias="X-API-Key")) -> str:
    app_id = await validate_api_key(x_api_key)
    if app_id is None:
        raise HTTPException(status_code=401, detail="Invalid API key")
    return app_id


def _now_ms() -> int:
    return int(datetime.now(timezone.utc).timestamp() * 1000)


def _week_ago_ms() -> int:
    return int((datetime.now(timezone.utc) - timedelta(days=7)).timestamp() * 1000)


@router.get("/metrics/summary")
async def summary(
    app_id: str = Depends(_get_app_id),
    from_ts: Optional[int] = Query(default=None, description="Start epoch ms (default: 7 days ago)"),
    to_ts: Optional[int] = Query(default=None, description="End epoch ms (default: now)"),
    app_version: Optional[str] = Query(default=None),
    network_type: Optional[str] = Query(default=None),
    device_model: Optional[str] = Query(default=None),
):
    return await metrics_service.get_summary(
        app_id,
        from_ts if from_ts is not None else _week_ago_ms(),
        to_ts if to_ts is not None else _now_ms(),
        app_version, network_type, device_model,
    )


@router.get("/metrics/startup")
async def startup_trend(
    app_id: str = Depends(_get_app_id),
    from_ts: Optional[int] = Query(default=None),
    to_ts: Optional[int] = Query(default=None),
    app_version: Optional[str] = Query(default=None),
    network_type: Optional[str] = Query(default=None),
    device_model: Optional[str] = Query(default=None),
):
    return await metrics_service.get_startup_trend(
        app_id,
        from_ts if from_ts is not None else _week_ago_ms(),
        to_ts if to_ts is not None else _now_ms(),
        app_version, network_type, device_model,
    )


@router.get("/metrics/screens")
async def screen_stats(
    app_id: str = Depends(_get_app_id),
    from_ts: Optional[int] = Query(default=None),
    to_ts: Optional[int] = Query(default=None),
    app_version: Optional[str] = Query(default=None),
    network_type: Optional[str] = Query(default=None),
    device_model: Optional[str] = Query(default=None),
):
    return await metrics_service.get_screen_stats(
        app_id,
        from_ts if from_ts is not None else _week_ago_ms(),
        to_ts if to_ts is not None else _now_ms(),
        app_version, network_type, device_model,
    )


@router.get("/metrics/network")
async def network_stats(
    app_id: str = Depends(_get_app_id),
    from_ts: Optional[int] = Query(default=None),
    to_ts: Optional[int] = Query(default=None),
    app_version: Optional[str] = Query(default=None),
    network_type: Optional[str] = Query(default=None),
    device_model: Optional[str] = Query(default=None),
):
    return await metrics_service.get_network_stats(
        app_id,
        from_ts if from_ts is not None else _week_ago_ms(),
        to_ts if to_ts is not None else _now_ms(),
        app_version, network_type, device_model,
    )


@router.get("/metrics/devices")
async def slowest_devices(
    app_id: str = Depends(_get_app_id),
    from_ts: Optional[int] = Query(default=None),
    to_ts: Optional[int] = Query(default=None),
    app_version: Optional[str] = Query(default=None),
    network_type: Optional[str] = Query(default=None),
    device_model: Optional[str] = Query(default=None),
):
    return await metrics_service.get_slowest_devices(
        app_id,
        from_ts if from_ts is not None else _week_ago_ms(),
        to_ts if to_ts is not None else _now_ms(),
        app_version, network_type, device_model,
    )


@router.get("/metrics/versions")
async def version_stats(
    app_id: str = Depends(_get_app_id),
    from_ts: Optional[int] = Query(default=None),
    to_ts: Optional[int] = Query(default=None),
    network_type: Optional[str] = Query(default=None),
    device_model: Optional[str] = Query(default=None),
):
    return await metrics_service.get_version_stats(
        app_id,
        from_ts if from_ts is not None else _week_ago_ms(),
        to_ts if to_ts is not None else _now_ms(),
        network_type, device_model,
    )


@router.get("/metrics/network-by-type")
async def network_by_type(
    app_id: str = Depends(_get_app_id),
    from_ts: Optional[int] = Query(default=None),
    to_ts: Optional[int] = Query(default=None),
    app_version: Optional[str] = Query(default=None),
    device_model: Optional[str] = Query(default=None),
):
    return await metrics_service.get_network_by_type(
        app_id,
        from_ts if from_ts is not None else _week_ago_ms(),
        to_ts if to_ts is not None else _now_ms(),
        app_version, device_model,
    )


@router.get("/metrics/health")
async def health_score(
    app_id: str = Depends(_get_app_id),
    from_ts: Optional[int] = Query(default=None),
    to_ts: Optional[int] = Query(default=None),
    app_version: Optional[str] = Query(default=None),
    network_type: Optional[str] = Query(default=None),
    device_model: Optional[str] = Query(default=None),
):
    return await metrics_service.get_health_score(
        app_id,
        from_ts if from_ts is not None else _week_ago_ms(),
        to_ts if to_ts is not None else _now_ms(),
        app_version, network_type, device_model,
    )


@router.get("/metrics/network/errors")
async def network_errors(
    app_id: str = Depends(_get_app_id),
    from_ts: Optional[int] = Query(default=None),
    to_ts: Optional[int] = Query(default=None),
    app_version: Optional[str] = Query(default=None),
    network_type: Optional[str] = Query(default=None),
    device_model: Optional[str] = Query(default=None),
    limit: int = Query(default=100, le=500),
):
    return await metrics_service.get_network_errors(
        app_id,
        from_ts if from_ts is not None else _week_ago_ms(),
        to_ts if to_ts is not None else _now_ms(),
        app_version, network_type, device_model, limit,
    )


@router.get("/metrics/traces")
async def trace_stats(
    app_id: str = Depends(_get_app_id),
    from_ts: Optional[int] = Query(default=None),
    to_ts: Optional[int] = Query(default=None),
    app_version: Optional[str] = Query(default=None),
    network_type: Optional[str] = Query(default=None),
    device_model: Optional[str] = Query(default=None),
):
    return await metrics_service.get_trace_stats(
        app_id,
        from_ts if from_ts is not None else _week_ago_ms(),
        to_ts if to_ts is not None else _now_ms(),
        app_version, network_type, device_model,
    )
