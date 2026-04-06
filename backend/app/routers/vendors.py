from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from math import radians, cos, sin, sqrt, atan2

from app.core.database import get_db
from app.models.db_models import Vendor
from app.models.schemas import VendorSchema

router = APIRouter()


def _haversine(lat1: float, lng1: float, lat2: float, lng2: float) -> float:
    """Return distance in km between two coordinates."""
    R = 6371.0
    dlat = radians(lat2 - lat1)
    dlng = radians(lng2 - lng1)
    a = sin(dlat / 2) ** 2 + cos(radians(lat1)) * cos(radians(lat2)) * sin(dlng / 2) ** 2
    return R * (2 * atan2(sqrt(a), sqrt(1 - a)))


@router.get("/vendors", response_model=list[VendorSchema])
async def get_vendors(
    lat: float = Query(default=21.15),
    lng: float = Query(default=79.09),
    radius_km: float = Query(default=50.0, le=200),
    db: AsyncSession = Depends(get_db),
) -> list[VendorSchema]:
    result = await db.execute(select(Vendor))
    all_vendors = result.scalars().all()

    nearby = [
        v for v in all_vendors
        if _haversine(lat, lng, v.lat, v.lng) <= radius_km
    ]

    # Return all if none are nearby (demo mode)
    return nearby or list(all_vendors)
