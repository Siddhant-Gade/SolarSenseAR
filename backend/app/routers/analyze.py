"""
Solar analysis pipeline:
  1. Resolve solar irradiance (PVGIS API or hardcoded table fallback)
  2. Compute energy output, financial metrics, and CO₂ impact
  3. Apply PM Surya Ghar subsidy slabs
  4. Generate AI narrative
  5. Persist scan to DB & return full AnalyzeResponse
"""
import uuid
from datetime import datetime

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.models.db_models import Scan
from app.models.schemas import AnalyzeRequest, AnalyzeResponse, MonthlyBreakdown, SubsidyBreakdownSchema
from app.services.pvgis_client import get_irradiance
from app.services.subsidy_api import calculate_subsidy, get_subsidy_breakdown

router = APIRouter()

PANEL_WATT = 550
COST_PER_KW = 60_000
GRID_RATE = 8.0
PR = 0.80
CO2_PER_KWH = 0.0816

MONTHLY_FACTORS = [0.82, 0.90, 1.05, 1.15, 1.18, 1.10, 0.98, 0.95, 1.00, 1.08, 0.90, 0.79]
MONTH_NAMES = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"]


@router.post("/analyze", response_model=AnalyzeResponse)
async def analyze(body: AnalyzeRequest, db: AsyncSession = Depends(get_db)) -> AnalyzeResponse:
    # ── 1. Get irradiance ────────────────────────────────────────────────────
    lat = body.location.get("lat", 21.15)
    lng = body.location.get("lng", 79.09)
    try:
        irradiance = await get_irradiance(lat, lng)
    except Exception:
        irradiance = 5.5  # fallback to national average

    # ── 2. Energy calculations ───────────────────────────────────────────────
    panel_count = body.panel_config.panel_count
    capacity_kw = (panel_count * PANEL_WATT) / 1000.0
    tilt_factor = 1.05 if body.roof_type == "sloped" else 1.0
    shadow_factor = 1 - body.shadow_loss_percent / 100.0

    annual_gen = capacity_kw * irradiance * 365 * PR * tilt_factor * shadow_factor
    monthly_avg = annual_gen / 12.0

    monthly_breakdown = [
        MonthlyBreakdown(month=MONTH_NAMES[i], kwh=int(monthly_avg * MONTHLY_FACTORS[i]))
        for i in range(12)
    ]
    yearly_gen = sum(m.kwh for m in monthly_breakdown)
    monthly_gen = int(monthly_avg)

    # ── 3. Financials ────────────────────────────────────────────────────────
    gross_cost = int(capacity_kw * COST_PER_KW)
    subsidy = calculate_subsidy(capacity_kw)
    net_cost = gross_cost - subsidy
    annual_savings = int(annual_gen * GRID_RATE)
    payback = round(net_cost / annual_savings, 2) if annual_savings > 0 else 0.0
    savings_25yr = annual_savings * 25 - net_cost

    # ── 4. Environment ───────────────────────────────────────────────────────
    co2 = int(annual_gen * CO2_PER_KWH)
    trees = int(annual_gen / 22.0)

    # Usage coverage
    monthly_usage = body.monthly_bill_inr / GRID_RATE
    coverage = min(100, int(monthly_gen / monthly_usage * 100)) if monthly_usage > 0 else 85

    # ── 5. Subsidy breakdown ─────────────────────────────────────────────────
    breakdown = get_subsidy_breakdown(capacity_kw)

    # ── 6. AI narrative ──────────────────────────────────────────────────────
    city = body.location_name or "your location"
    narrative = (
        f"Based on {city}'s solar irradiance of {irradiance:.1f} kWh/m²/day, your "
        f"{capacity_kw:.1f} kW system with {panel_count} panels will generate approximately "
        f"{monthly_gen} kWh of electricity every month. "
        f"After the PM Surya Ghar subsidy of ₹{subsidy:,}, your net investment is just "
        f"₹{net_cost:,} — which pays for itself in {payback:.1f} years. "
        f"Over 25 years, you stand to save ₹{savings_25yr:,}. "
        f"Apply at pmsuryaghar.gov.in to claim your subsidy."
    )

    # ── 7. Persist scan ──────────────────────────────────────────────────────
    scan_id = str(uuid.uuid4())
    scan = Scan(
        id=scan_id,
        user_id="anonymous",  # replace with JWT user_id when auth middleware is added
        location_name=body.location_name,
        location_lat=lat,
        location_lng=lng,
        roof_type=body.roof_type,
        panel_count=panel_count,
        system_kw=capacity_kw,
        monthly_gen_units=monthly_gen,
        annual_gen_units=yearly_gen,
        gross_cost_inr=gross_cost,
        subsidy_inr=subsidy,
        net_cost_inr=net_cost,
        annual_savings_inr=annual_savings,
        payback_years=payback,
        savings_25yr_inr=savings_25yr,
        co2_kg_annual=co2,
        shadow_loss_percent=body.shadow_loss_percent,
        irradiance_kwh_m2_day=irradiance,
        ai_narrative=narrative,
        created_at=datetime.utcnow()
    )
    db.add(scan)
    await db.commit()

    return AnalyzeResponse(
        scan_id=scan_id,
        panel_count=panel_count,
        system_kw=round(capacity_kw, 2),
        monthly_gen_kwh=monthly_gen,
        yearly_gen_kwh=yearly_gen,
        monthly_breakdown=monthly_breakdown,
        gross_cost_inr=gross_cost,
        subsidy_inr=subsidy,
        net_cost_inr=net_cost,
        annual_savings_inr=annual_savings,
        payback_years=payback,
        savings_25yr_inr=savings_25yr,
        shadow_loss_percent=body.shadow_loss_percent,
        co2_saved_kg_per_year=co2,
        trees_equivalent=trees,
        usage_coverage_percent=coverage,
        irradiance_kwh_m2_day=round(irradiance, 2),
        subsidy_breakdown=SubsidyBreakdownSchema(**breakdown),
        ai_narrative=narrative,
    )
