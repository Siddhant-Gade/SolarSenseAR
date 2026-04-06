from __future__ import annotations

from pydantic import BaseModel, EmailStr, Field


# ── Auth ──────────────────────────────────────────────────────────────────────

class UserCreate(BaseModel):
    email: EmailStr
    name: str = Field(min_length=2, max_length=100)
    password: str = Field(min_length=6)


class UserLogin(BaseModel):
    email: EmailStr
    password: str


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user_id: str
    name: str
    email: str


# ── Analysis (REST /api/v1/analyze) ──────────────────────────────────────────

class PanelConfig(BaseModel):
    panel_count: int = Field(ge=1, le=100)
    total_kw: float = 0.0
    roof_area_sq_ft: float | None = None


class AnalyzeRequest(BaseModel):
    image_base64: str | None = None
    panel_config: PanelConfig
    location: dict[str, float]               # {"lat": ..., "lng": ...}
    location_name: str = "Unknown"
    roof_type: str = "flat"
    monthly_bill_inr: float = 2000.0
    shadow_loss_percent: float = Field(default=5.0, ge=0, le=50)


class SubsidyBreakdownSchema(BaseModel):
    up_to_1kw: int
    one_to_2kw: int
    above_2kw: int
    total_subsidy: int
    scheme_name: str


class MonthlyBreakdown(BaseModel):
    month: str
    kwh: int


class AnalyzeResponse(BaseModel):
    scan_id: str
    panel_count: int
    system_kw: float
    monthly_gen_kwh: int
    yearly_gen_kwh: int
    monthly_breakdown: list[MonthlyBreakdown]
    gross_cost_inr: int
    subsidy_inr: int
    net_cost_inr: int
    annual_savings_inr: int
    payback_years: float
    savings_25yr_inr: int
    shadow_loss_percent: float
    co2_saved_kg_per_year: int
    trees_equivalent: int
    usage_coverage_percent: int
    irradiance_kwh_m2_day: float
    subsidy_breakdown: SubsidyBreakdownSchema
    ai_narrative: str


# ── Android-compatible schemas (/generate-report, /vendors-nearby) ────────────

class GenerateReportRequest(BaseModel):
    """Matches Android ReportRequest.kt exactly."""
    latitude: float
    longitude: float
    panel_count: int = Field(ge=1, le=100)
    panel_watt: int | None = 550
    roof_type: str = "flat"
    monthly_bill_inr: float = 2000.0
    shadow_loss_percent: float = Field(default=5.0, ge=0, le=50)
    location_name: str | None = None
    state: str | None = None


class SubsidyBreakdownAndroid(BaseModel):
    """Matches Android SubsidyBreakdown.kt (uses 1_to_2kw key)."""
    up_to_1kw: int
    one_to_2kw: int = Field(alias="1_to_2kw", default=0)
    above_2kw: int

    model_config = {"populate_by_name": True}


class GenerateReportResponse(BaseModel):
    """Matches Android ReportResponse.kt exactly."""
    scan_id: str = ""
    capacity_kw: float
    monthly_generation_units: int
    annual_generation_units: int
    installation_cost_inr: int
    subsidy_inr: int
    net_cost_inr: int
    annual_savings_inr: int
    payback_years: float
    savings_25yr_inr: int
    co2_kg_annual: int
    trees_equivalent: int
    usage_coverage_percent: int
    irradiance_kwh_m2_day: float
    ai_narrative: str
    subsidy_scheme: str
    subsidy_breakdown: SubsidyBreakdownSchema


class VendorNearbyRequest(BaseModel):
    """Matches Android VendorNearbyRequest data class."""
    latitude: float
    longitude: float
    radius_km: float = Field(default=25.0, alias="radiusKm")

    model_config = {"populate_by_name": True}


# ── Vendor ────────────────────────────────────────────────────────────────────

class VendorSchema(BaseModel):
    id: str
    name: str
    city: str
    state: str
    contact_phone: str
    rating: float
    reviews: int
    price_per_kw_inr: int
    lat: float
    lng: float
    verified: bool
    years_in_business: int

    model_config = {"from_attributes": True}


# ── Report ────────────────────────────────────────────────────────────────────

class ScanSummary(BaseModel):
    id: str
    location_name: str
    panel_count: int
    system_kw: float
    net_cost_inr: int
    payback_years: float
    created_at: str

    model_config = {"from_attributes": True}
