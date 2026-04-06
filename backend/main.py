"""
SolarSense AR — FastAPI Backend
Entry point: uvicorn main:app --reload
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager

from app.core.database import create_tables
from app.routers import auth, analyze, vendors, report


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup — create tables if they don't exist
    await create_tables()
    yield
    # Shutdown (cleanup if needed)


app = FastAPI(
    title="SolarSense AR API",
    description=(
        "Real-time AI-powered solar panel planning backend. "
        "Integrates PVGIS irradiance data, PM Surya Ghar subsidy slabs, "
        "YOLOv8 obstacle detection, and Depth Anything V2."
    ),
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(auth.router, prefix="/api/v1/auth", tags=["Authentication"])
app.include_router(analyze.router, prefix="/api/v1", tags=["Analysis"])
app.include_router(vendors.router, prefix="/api/v1", tags=["Vendors"])
app.include_router(report.router, prefix="/api/v1", tags=["Reports"])


@app.get("/health")
async def health():
    return {"status": "ok", "service": "SolarSense AR API v1.0"}
