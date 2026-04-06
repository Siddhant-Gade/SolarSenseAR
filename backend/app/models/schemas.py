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


# ── Analysis ──────────────────────────────────────────────────────────────────

class PanelConfig(BaseModel):
    panel_count: int = Field(ge=1, le=100)
    total_kw: float
    roof_area_sq_ft: float | None = None


class AnalyzeRequest(BaseModel):
    image_base64: str | None = None          # Optional AR snapshot
    panel_config: PanelConfig
    location: dict[str, float]               # {"lat": ..., "lng": ...}
    location_name: str = "Unknown"
    roof_type: str = "flat"                  # "flat" | "sloped"
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
