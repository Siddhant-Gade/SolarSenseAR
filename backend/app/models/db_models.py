import uuid
from datetime import datetime

from sqlalchemy import Boolean, Float, Integer, String, Text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.core.database import Base


class User(Base):
    __tablename__ = "users"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    email: Mapped[str] = mapped_column(String(255), unique=True, index=True, nullable=False)
    name: Mapped[str] = mapped_column(String(255), nullable=False)
    hashed_password: Mapped[str | None] = mapped_column(String(255), nullable=True)
    auth_provider: Mapped[str] = mapped_column(String(50), default="email")
    created_at: Mapped[datetime] = mapped_column(default=datetime.utcnow)

    scans: Mapped[list["Scan"]] = relationship("Scan", back_populates="user", lazy="selectin",
                                                foreign_keys="[Scan.user_id]",
                                                primaryjoin="User.id == Scan.user_id")


class Scan(Base):
    __tablename__ = "scans"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))

    # Nullable FK — supports anonymous/guest scans without a User record
    user_id: Mapped[str] = mapped_column(String(36), nullable=False, default="anonymous", index=True)

    location_name: Mapped[str] = mapped_column(String(255), default="")
    location_lat: Mapped[float] = mapped_column(Float, default=0.0)
    location_lng: Mapped[float] = mapped_column(Float, default=0.0)
    roof_type: Mapped[str] = mapped_column(String(20), default="flat")

    panel_count: Mapped[int] = mapped_column(Integer, default=0)
    system_kw: Mapped[float] = mapped_column(Float, default=0.0)
    monthly_gen_units: Mapped[int] = mapped_column(Integer, default=0)
    annual_gen_units: Mapped[int] = mapped_column(Integer, default=0)

    gross_cost_inr: Mapped[int] = mapped_column(Integer, default=0)
    subsidy_inr: Mapped[int] = mapped_column(Integer, default=0)
    net_cost_inr: Mapped[int] = mapped_column(Integer, default=0)
    annual_savings_inr: Mapped[int] = mapped_column(Integer, default=0)
    payback_years: Mapped[float] = mapped_column(Float, default=0.0)
    savings_25yr_inr: Mapped[int] = mapped_column(Integer, default=0)

    co2_kg_annual: Mapped[int] = mapped_column(Integer, default=0)
    shadow_loss_percent: Mapped[float] = mapped_column(Float, default=0.0)
    irradiance_kwh_m2_day: Mapped[float] = mapped_column(Float, default=5.5)

    ai_narrative: Mapped[str] = mapped_column(Text, default="")
    ar_snapshot_url: Mapped[str] = mapped_column(String(1024), default="")
    report_pdf_url: Mapped[str] = mapped_column(String(1024), default="")

    created_at: Mapped[datetime] = mapped_column(default=datetime.utcnow, index=True)

    # Optional relationship — only populated when user_id matches a real User
    user: Mapped["User | None"] = relationship(
        "User", back_populates="scans",
        foreign_keys=[user_id],
        primaryjoin="Scan.user_id == User.id",
    )


class Vendor(Base):
    __tablename__ = "vendors"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    name: Mapped[str] = mapped_column(String(255), nullable=False)
    city: Mapped[str] = mapped_column(String(255), nullable=False)
    state: Mapped[str] = mapped_column(String(255), default="")
    contact_phone: Mapped[str] = mapped_column(String(20), default="")
    rating: Mapped[float] = mapped_column(Float, default=4.5)
    reviews: Mapped[int] = mapped_column(Integer, default=0)
    price_per_kw_inr: Mapped[int] = mapped_column(Integer, default=60000)
    lat: Mapped[float] = mapped_column(Float, default=0.0)
    lng: Mapped[float] = mapped_column(Float, default=0.0)
    verified: Mapped[bool] = mapped_column(Boolean, default=True, index=True)
    years_in_business: Mapped[int] = mapped_column(Integer, default=5)
