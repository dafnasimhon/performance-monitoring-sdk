from pydantic import BaseModel
from typing import Optional


class EventModel(BaseModel):
    eventId: str
    sessionId: str
    eventType: str
    eventName: str
    startTime: Optional[int] = None
    endTime: Optional[int] = None
    durationMs: int
    timestamp: int
    appVersion: str
    androidVersion: str
    deviceModel: str
    manufacturer: str
    networkType: str
    networkSubtype: Optional[str] = ""
    availableRamMb: Optional[int] = 0
    isBatterySaverActive: Optional[bool] = False
    apiLevel: Optional[int] = 0
    isEmulator: Optional[bool] = False
    appPackageName: Optional[str] = ""
    endpoint: Optional[str] = None
    method: Optional[str] = None
    statusCode: Optional[int] = None
    success: Optional[bool] = None


class BatchRequest(BaseModel):
    sentAt: int
    events: list[EventModel]


class BatchResponse(BaseModel):
    status: str
    accepted: int
