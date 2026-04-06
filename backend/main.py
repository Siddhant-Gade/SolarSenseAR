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
    await create_tables()
    yield


app = FastAPI(
    title="SolarSense AR API",
    description=(
        "Real-time AI-powered solar panel planning backend. "
        "Integrates PVGIS irradiance data, PM Surya Ghar subsidy slabs, "
        "and full financial analysis."
    ),
    version="1.1.0",
    lifespan=lifespan,
    docs_url="/docs",
    redoc_url="/redoc",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Android app routes (no prefix — matches BuildConfig.BACKEND_URL directly)
app.include_router(analyze.router, tags=["Analysis"])   # /generate-report
app.include_router(vendors.router, tags=["Vendors"])    # /vendors-nearby

# REST API routes
app.include_router(auth.router,    prefix="/api/v1/auth",  tags=["Auth"])
app.include_router(analyze.router, prefix="/api/v1",       tags=["Analysis v1"])
app.include_router(vendors.router, prefix="/api/v1",       tags=["Vendors v1"])
app.include_router(report.router,  prefix="/api/v1",       tags=["Reports"])


@app.get("/health", tags=["Health"])
async def health():
    return {"status": "ok", "service": "SolarSense AR API v1.1"}
