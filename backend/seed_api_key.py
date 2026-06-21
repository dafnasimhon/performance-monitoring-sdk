"""
Run once to seed the dev API key into MongoDB.

Usage:
    python seed_api_key.py
"""
import asyncio
import hashlib
import os
from datetime import datetime, timezone

from dotenv import load_dotenv
from motor.motor_asyncio import AsyncIOMotorClient

load_dotenv()

API_KEY = "dev-api-key"
APP_NAME = "Demo Android App"


async def seed():
    uri = os.getenv("MONGODB_URI")
    client = AsyncIOMotorClient(uri)
    db = client["perfsdk"]

    key_hash = hashlib.sha256(API_KEY.encode()).hexdigest()
    existing = await db["applications"].find_one({"apiKeyHash": key_hash})
    if existing:
        print(f"API key already seeded (appId: {existing['_id']})")
        return

    result = await db["applications"].insert_one({
        "name": APP_NAME,
        "apiKeyHash": key_hash,
        "createdAt": datetime.now(timezone.utc),
    })
    print(f"Seeded API key '{API_KEY}' -> appId: {result.inserted_id}")
    client.close()


asyncio.run(seed())
