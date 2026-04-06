from fastapi import APIRouter, Depends, HTTPException
from fastapi.responses import StreamingResponse
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from io import BytesIO

from app.core.database import get_db
from app.models.db_models import Scan
from app.models.schemas import ScanSummary
from app.services.pdf_builder import generate_report_pdf

router = APIRouter()


@router.get("/report/{scan_id}", response_model=ScanSummary)
async def get_report(scan_id: str, db: AsyncSession = Depends(get_db)) -> ScanSummary:
    result = await db.execute(select(Scan).where(Scan.id == scan_id))
    scan: Scan | None = result.scalar_one_or_none()
    if not scan:
        raise HTTPException(status_code=404, detail="Scan not found")
    return ScanSummary(
        id=scan.id,
        location_name=scan.location_name,
        panel_count=scan.panel_count,
        system_kw=scan.system_kw,
        net_cost_inr=scan.net_cost_inr,
        payback_years=scan.payback_years,
        created_at=scan.created_at.isoformat()
    )


@router.get("/report/{scan_id}/pdf")
async def download_pdf(scan_id: str, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(Scan).where(Scan.id == scan_id))
    scan: Scan | None = result.scalar_one_or_none()
    if not scan:
        raise HTTPException(status_code=404, detail="Scan not found")

    scan_data = {
        "location_name": scan.location_name,
        "panel_count": scan.panel_count,
        "system_kw": scan.system_kw,
        "monthly_gen_units": scan.monthly_gen_units,
        "annual_gen_units": scan.annual_gen_units,
        "gross_cost_inr": scan.gross_cost_inr,
        "subsidy_inr": scan.subsidy_inr,
        "net_cost_inr": scan.net_cost_inr,
        "annual_savings_inr": scan.annual_savings_inr,
        "payback_years": scan.payback_years,
        "savings_25yr_inr": scan.savings_25yr_inr,
        "co2_kg_annual": scan.co2_kg_annual,
        "irradiance_kwh_m2_day": scan.irradiance_kwh_m2_day,
        "ai_narrative": scan.ai_narrative,
        "created_at": scan.created_at.isoformat()
    }
    pdf_bytes = generate_report_pdf(scan_data)
    return StreamingResponse(
        BytesIO(pdf_bytes),
        media_type="application/pdf",
        headers={"Content-Disposition": f"attachment; filename=solarsense_{scan_id[:8]}.pdf"}
    )


@router.get("/user/scans", response_model=list[ScanSummary])
async def list_user_scans(db: AsyncSession = Depends(get_db)) -> list[ScanSummary]:
    """Returns all scans (add JWT middleware to filter by user_id in production)."""
    result = await db.execute(select(Scan).order_by(Scan.created_at.desc()).limit(20))
    scans = result.scalars().all()
    return [
        ScanSummary(
            id=s.id,
            location_name=s.location_name,
            panel_count=s.panel_count,
            system_kw=s.system_kw,
            net_cost_inr=s.net_cost_inr,
            payback_years=s.payback_years,
            created_at=s.created_at.isoformat()
        )
        for s in scans
    ]
