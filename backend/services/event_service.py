import hashlib
from datetime import datetime, timezone

from config.database import get_db
from models.schemas import EventModel


def _hash_key(api_key: str) -> str:
    return hashlib.sha256(api_key.encode()).hexdigest()


async def validate_api_key(api_key: str) -> str | None:
    """Returns the appId if the key is valid, None otherwise."""
    db = get_db()
    key_hash = _hash_key(api_key)
    app = await db["applications"].find_one({"apiKeyHash": key_hash})
    if app is None:
        return None
    return str(app["_id"])


async def save_raw_events(app_id: str, events: list[EventModel]) -> int:
    db = get_db()
    docs = [
        {**event.model_dump(), "appId": app_id, "receivedAt": datetime.now(timezone.utc)}
        for event in events
    ]
    if docs:
        await db["raw_events"].insert_many(docs, ordered=False)
    return len(docs)
