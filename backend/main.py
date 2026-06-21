from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from routes.events import router as events_router
from routes.metrics import router as metrics_router

app = FastAPI(title="PerfSDK Backend", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(events_router, prefix="/api/v1")
app.include_router(metrics_router, prefix="/api/v1")


@app.get("/health")
async def health():
    return {"status": "ok"}
