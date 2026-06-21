"""Run once to create MongoDB indexes for the metrics queries."""
import asyncio
import os

import pymongo
from dotenv import load_dotenv
from motor.motor_asyncio import AsyncIOMotorClient

load_dotenv()


async def create_indexes():
    client = AsyncIOMotorClient(os.getenv("MONGODB_URI"))
    db = client["perfsdk"]

    await db["raw_events"].create_index(
        [("appId", pymongo.ASCENDING), ("timestamp", pymongo.DESCENDING)],
        name="appId_timestamp",
    )
    await db["raw_events"].create_index(
        [("appId", pymongo.ASCENDING), ("eventType", pymongo.ASCENDING), ("timestamp", pymongo.DESCENDING)],
        name="appId_eventType_timestamp",
    )

    print("Indexes created on raw_events")
    client.close()


asyncio.run(create_indexes())
