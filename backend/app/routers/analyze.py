"""
Solar analysis pipeline:
  POST /generate-report   ← Android app endpoint (matches ApiService.kt)
  POST /api/v1/analyze    ← REST-style alias

  1. Resolve irradiance (PVGIS or lookup table)
  2. Compute energy, financials, CO₂
  3. Apply PM Surya Ghar subsidy slabs
  4. Build AI narrative
  5. Persist scan (guest-safe — no FK violation)
  6. Return AnalyzeResponse
"""
import uuid
from datetime import datetime

from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import settings
from app.core.database import get_db
from app.core.security import get_current_user_id
from app.models.db_models import Scan
from app.models.schemas import (
    AnalyzeRequest,
    AnalyzeResponse,
    GenerateReportRequest,
    GenerateReportResponse,
    MonthlyBreakdown,
    SubsidyBreakdownSchema,
)
from app.services.pvgis_client import get_irradiance
from app.services.subsidy_api import calculate_subsidy, get_subsidy_breakdown

router = APIRouter()

# Monthly seasonal factors (India — Jan through Dec)
MONTHLY_FACTORS = [0.82, 0.90, 1.05, 1.15, 1.18, 1.10, 0.98, 0.95, 1.00, 1.08, 0.90, 0.79]
MONTH_NAMES = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]


def _compute(
    panel_count: int,
    panel_watt: int,
    roof_type: str,
    shadow_loss_percent: float,
    monthly_bill_inr: float,
    irradiance: float,
    location_name: str,
) -> dict:
    """Pure computation — returns all financial + energy fields."""
    capacity_kw = (panel_count * panel_watt) / 1000.0
    tilt_factor = 1.05 if roof_type == "sloped" else 1.0
    shadow_factor = 1.0 - shadow_loss_percent / 100.0

    annual_gen = (
        capacity_kw * irradiance * 365
        * settings.PERFORMANCE_RATIO * tilt_factor * shadow_factor
    )
    monthly_avg = annual_gen / 12.0

    monthly_breakdown = [
        MonthlyBreakdown(month=MONTH_NAMES[i], kwh=int(monthly_avg * MONTHLY_FACTORS[i]))
        for i in range(12)
    ]
    yearly_gen = sum(m.kwh for m in monthly_breakdown)
    monthly_gen = int(monthly_avg)

    gross_cost = int(capacity_kw * settings.COST_PER_KW_INR)
    subsidy = calculate_subsidy(capacity_kw)
    net_cost = gross_cost - subsidy
    annual_savings = int(annual_gen * settings.GRID_RATE_PER_UNIT)
    payback = round(net_cost / annual_savings, 2) if annual_savings > 0 else 0.0
    savings_25yr = int(annual_savings * 25 - net_cost)

    co2 = int(annual_gen * settings.CO2_PER_KWH_KG)
    trees = max(1, int(annual_gen / 22.0))

    monthly_usage_kwh = monthly_bill_inr / settings.GRID_RATE_PER_UNIT
    coverage = min(100, int(monthly_gen / monthly_usage_kwh * 100)) if monthly_usage_kwh > 0 else 85

    breakdown = get_subsidy_breakdown(capacity_kw)

    city = location_name or "your location"
    narrative = (
        f"Based on {city}'s solar irradiance of {irradiance:.1f} kWh/m²/day, your "
        f"{capacity_kw:.1f} kW system with {panel_count} panels will generate approximately "
        f"{monthly_gen} kWh every month — covering {coverage}% of your electricity bill. "
        f"After the PM Surya Ghar subsidy of ₹{subsidy:,}, your net investment is just "
        f"₹{net_cost:,} — which pays for itself in {payback:.1f} years. "
        f"Over 25 years, you stand to save ₹{savings_25yr:,}. "
        f"Apply at pmsuryaghar.gov.in to claim your subsidy."
    )

    return dict(
        capacity_kw=round(capacity_kw, 2),
        monthly_gen=monthly_gen,
        yearly_gen=yearly_gen,
        monthly_breakdown=monthly_breakdown,
        gross_cost=gross_cost,
        subsidy=subsidy,
        net_cost=net_cost,
        annual_savings=annual_savings,
        payback=payback,
        savings_25yr=savings_25yr,
        co2=co2,
        trees=trees,
        coverage=coverage,
        breakdown=breakdown,
        narrative=narrative,
    )


async def _persist_scan(
    db: AsyncSession,
    user_id: str,
    location_name: str,
    lat: float,
    lng: float,
    roof_type: str,
    panel_count: int,
    shadow_loss_percent: float,
    irradiance: float,
    c: dict,
) -> str:
    scan_id = str(uuid.uuid4())
    scan = Scan(
        id=scan_id,
        user_id=user_id,
        location_name=location_name,
        location_lat=lat,
        location_lng=lng,
        roof_type=roof_type,
        panel_count=panel_count,
        system_kw=c["capacity_kw"],
        monthly_gen_units=c["monthly_gen"],
        annual_gen_units=c["yearly_gen"],
        gross_cost_inr=c["gross_cost"],
        subsidy_inr=c["subsidy"],
        net_cost_inr=c["net_cost"],
        annual_savings_inr=c["annual_savings"],
        payback_years=c["payback"],
        savings_25yr_inr=c["savings_25yr"],
        co2_kg_annual=c["co2"],
        shadow_loss_percent=shadow_loss_percent,
        irradiance_kwh_m2_day=irradiance,
        ai_narrative=c["narrative"],
        created_at=datetime.utcnow(),
    )
    db.add(scan)
    await db.commit()
    return scan_id


# ─────────────────────────────────────────────────────────────────────────────
# ENDPOINT 1: /generate-report  (Android ApiService.kt calls this)
# ─────────────────────────────────────────────────────────────────────────────

@router.post("/generate-report", response_model=GenerateReportResponse)
async def generate_report(
    body: GenerateReportRequest,
    db: AsyncSession = Depends(get_db),
    user_id: str = Depends(get_current_user_id),
) -> GenerateReportResponse:
    """Primary endpoint consumed by the Android app."""
    lat, lng = body.latitude, body.longitude
    panel_watt = body.panel_watt or settings.PANEL_WATT

    try:
        irradiance = await get_irradiance(lat, lng, city_hint=body.location_name or "")
    except Exception:
        irradiance = 5.5

    c = _compute(
        panel_count=body.panel_count,
        panel_watt=panel_watt,
        roof_type=body.roof_type,
        shadow_loss_percent=body.shadow_loss_percent,
        monthly_bill_inr=body.monthly_bill_inr,
        irradiance=irradiance,
        location_name=body.location_name or "",
    )

    scan_id = await _persist_scan(
        db=db, user_id=user_id,
        location_name=body.location_name or "Unknown",
        lat=lat, lng=lng,
        roof_type=body.roof_type,
        panel_count=body.panel_count,
        shadow_loss_percent=body.shadow_loss_percent,
        irradiance=irradiance,
        c=c,
    )

    bd = c["breakdown"]
    return GenerateReportResponse(
        scan_id=scan_id,
        capacity_kw=c["capacity_kw"],
        monthly_generation_units=c["monthly_gen"],
        annual_generation_units=c["yearly_gen"],
        installation_cost_inr=c["gross_cost"],
        subsidy_inr=c["subsidy"],
        net_cost_inr=c["net_cost"],
        annual_savings_inr=c["annual_savings"],
        payback_years=c["payback"],
        savings_25yr_inr=c["savings_25yr"],
        co2_kg_annual=c["co2"],
        trees_equivalent=c["trees"],
        usage_coverage_percent=c["coverage"],
        irradiance_kwh_m2_day=round(irradiance, 2),
        ai_narrative=c["narrative"],
        subsidy_scheme="PM Surya Ghar Muft Bijli Yojana",
        subsidy_breakdown=SubsidyBreakdownSchema(
            up_to_1kw=bd["up_to_1kw"],
            one_to_2kw=bd["one_to_2kw"],
            above_2kw=bd["above_2kw"],
            total_subsidy=bd["total_subsidy"],
            scheme_name=bd["scheme_name"],
        ),
    )


# ─────────────────────────────────────────────────────────────────────────────
# ENDPOINT 2: /api/v1/analyze  (REST alias, same logic)
# ─────────────────────────────────────────────────────────────────────────────

@router.post("/analyze", response_model=AnalyzeResponse)
async def analyze(
    body: AnalyzeRequest,
    db: AsyncSession = Depends(get_db),
    user_id: str = Depends(get_current_user_id),
) -> AnalyzeResponse:
    lat = body.location.get("lat", 21.15)
    lng = body.location.get("lng", 79.09)
    panel_watt = settings.PANEL_WATT

    try:
        irradiance = await get_irradiance(lat, lng, city_hint=body.location_name)
    except Exception:
        irradiance = 5.5

    c = _compute(
        panel_count=body.panel_config.panel_count,
        panel_watt=panel_watt,
        roof_type=body.roof_type,
        shadow_loss_percent=body.shadow_loss_percent,
        monthly_bill_inr=body.monthly_bill_inr,
        irradiance=irradiance,
        location_name=body.location_name,
    )

    scan_id = await _persist_scan(
        db=db, user_id=user_id,
        location_name=body.location_name,
        lat=lat, lng=lng,
        roof_type=body.roof_type,
        panel_count=body.panel_config.panel_count,
        shadow_loss_percent=body.shadow_loss_percent,
        irradiance=irradiance,
        c=c,
    )

    bd = c["breakdown"]
    return AnalyzeResponse(
        scan_id=scan_id,
        panel_count=body.panel_config.panel_count,
        system_kw=c["capacity_kw"],
        monthly_gen_kwh=c["monthly_gen"],
        yearly_gen_kwh=c["yearly_gen"],
        monthly_breakdown=c["monthly_breakdown"],
        gross_cost_inr=c["gross_cost"],
        subsidy_inr=c["subsidy"],
        net_cost_inr=c["net_cost"],
        annual_savings_inr=c["annual_savings"],
        payback_years=c["payback"],
        savings_25yr_inr=c["savings_25yr"],
        shadow_loss_percent=body.shadow_loss_percent,
        co2_saved_kg_per_year=c["co2"],
        trees_equivalent=c["trees"],
        usage_coverage_percent=c["coverage"],
        irradiance_kwh_m2_day=round(irradiance, 2),
        subsidy_breakdown=SubsidyBreakdownSchema(**bd),
        ai_narrative=c["narrative"],
    )
