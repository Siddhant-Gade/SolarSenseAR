from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker, create_async_engine
from sqlalchemy.orm import DeclarativeBase

from app.core.config import settings

# SQLite needs check_same_thread=False; Postgres can use pool_pre_ping
connect_args = {"check_same_thread": False} if "sqlite" in settings.DATABASE_URL else {}

engine = create_async_engine(
    settings.DATABASE_URL,
    echo=False,
    pool_pre_ping=True,
    connect_args=connect_args,
)
AsyncSessionLocal = async_sessionmaker(engine, expire_on_commit=False, class_=AsyncSession)


class Base(DeclarativeBase):
    pass


async def create_tables() -> None:
    """Create all tables on startup and seed vendor data if empty."""
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)

    # Seed vendors if table is empty
    from sqlalchemy.future import select
    from app.models.db_models import Vendor
    async with AsyncSessionLocal() as session:
        result = await session.execute(select(Vendor).limit(1))
        if not result.scalar_one_or_none():
            await _seed_vendors(session)


async def _seed_vendors(session: AsyncSession) -> None:
    """Insert sample vendors for demo/testing."""
    from app.models.db_models import Vendor
    import uuid

    vendors = [
        Vendor(id=str(uuid.uuid4()), name="SunPower Solutions", city="Nagpur",
               state="Maharashtra", contact_phone="+91-9876543210", rating=4.7,
               reviews=142, price_per_kw_inr=58000, lat=21.1458, lng=79.0882,
               verified=True, years_in_business=8),
        Vendor(id=str(uuid.uuid4()), name="GreenRay Energy", city="Nagpur",
               state="Maharashtra", contact_phone="+91-9876543211", rating=4.5,
               reviews=89, price_per_kw_inr=55000, lat=21.1322, lng=79.0672,
               verified=True, years_in_business=5),
        Vendor(id=str(uuid.uuid4()), name="SolarFirst India", city="Nagpur",
               state="Maharashtra", contact_phone="+91-9876543212", rating=4.8,
               reviews=203, price_per_kw_inr=62000, lat=21.1564, lng=79.1102,
               verified=True, years_in_business=10),
        Vendor(id=str(uuid.uuid4()), name="Tata Power Solar", city="Mumbai",
               state="Maharashtra", contact_phone="+91-9876543213", rating=4.9,
               reviews=512, price_per_kw_inr=65000, lat=19.0760, lng=72.8777,
               verified=True, years_in_business=15),
        Vendor(id=str(uuid.uuid4()), name="Adani Solar", city="Ahmedabad",
               state="Gujarat", contact_phone="+91-9876543214", rating=4.6,
               reviews=389, price_per_kw_inr=57000, lat=23.0225, lng=72.5714,
               verified=True, years_in_business=12),
        Vendor(id=str(uuid.uuid4()), name="Waaree Energies", city="Pune",
               state="Maharashtra", contact_phone="+91-9876500001", rating=4.7,
               reviews=275, price_per_kw_inr=59000, lat=18.5204, lng=73.8567,
               verified=True, years_in_business=9),
        Vendor(id=str(uuid.uuid4()), name="Vikram Solar", city="Jaipur",
               state="Rajasthan", contact_phone="+91-9876500002", rating=4.6,
               reviews=198, price_per_kw_inr=60000, lat=26.9124, lng=75.7873,
               verified=True, years_in_business=7),
        Vendor(id=str(uuid.uuid4()), name="Luminous Solar", city="Delhi",
               state="Delhi", contact_phone="+91-9876500003", rating=4.4,
               reviews=320, price_per_kw_inr=56000, lat=28.7041, lng=77.1025,
               verified=True, years_in_business=11),
    ]
    session.add_all(vendors)
    await session.commit()


async def get_db() -> AsyncSession:
    """FastAPI dependency: yields an async DB session."""
    async with AsyncSessionLocal() as session:
        yield session
