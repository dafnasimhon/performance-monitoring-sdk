from fastapi import APIRouter, Header, HTTPException

from models.schemas import BatchRequest, BatchResponse
from services.event_service import validate_api_key, save_raw_events

router = APIRouter()


@router.post("/events/batch", response_model=BatchResponse)
async def receive_batch(
    request: BatchRequest,
    x_api_key: str = Header(..., alias="X-API-Key"),
):
    app_id = await validate_api_key(x_api_key)
    if app_id is None:
        raise HTTPException(status_code=401, detail="Invalid API key")

    accepted = await save_raw_events(app_id, request.events)
    return BatchResponse(status="success", accepted=accepted)
