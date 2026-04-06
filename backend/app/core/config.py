from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

    # Database
    DATABASE_URL: str = "sqlite+aiosqlite:///./solarsense.db"

    # JWT
    SECRET_KEY: str = "change-me-in-production-use-openssl-rand-hex-32"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 7  # 7 days

    # AWS S3 (optional — for snapshot uploads)
    AWS_ACCESS_KEY_ID: str = ""
    AWS_SECRET_ACCESS_KEY: str = ""
    S3_BUCKET: str = "solarsense-snapshots"
    S3_REGION: str = "ap-south-1"

    # Solar constants (INR)
    COST_PER_KW_INR: int = 60_000
    GRID_RATE_PER_UNIT: float = 8.0
    PANEL_WATT: int = 550
    PERFORMANCE_RATIO: float = 0.80
    CO2_PER_KWH_KG: float = 0.0816


settings = Settings()
