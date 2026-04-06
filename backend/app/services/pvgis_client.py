"""
PVGIS API client — fetches solar irradiance data for a given lat/lng.
Endpoint: https://re.jrc.ec.europa.eu/api/v5_2/PVcalc
"""
import httpx

# Fallback lookup table (avg daily irradiance kWh/m²/day)
CITY_IRRADIANCE: dict[str, float] = {
    "nagpur": 5.80, "mumbai": 5.52, "delhi": 5.61, "bangalore": 5.83,
    "hyderabad": 5.82, "chennai": 5.94, "kolkata": 5.00, "pune": 5.55,
    "ahmedabad": 5.99, "jaipur": 5.97, "lucknow": 5.40, "bhopal": 5.65,
    "surat": 5.70, "indore": 5.69, "patna": 5.30, "chandigarh": 5.20,
    "coimbatore": 5.88, "kochi": 5.10, "vizag": 5.70,
}

PVGIS_URL = "https://re.jrc.ec.europa.eu/api/v5_2/PVcalc"


async def get_irradiance(lat: float, lng: float, city_hint: str = "") -> float:
    """
    Returns average daily solar irradiance kWh/m²/day for the given coordinates.
    Tries PVGIS API first; falls back to city lookup table or national average (5.5).
    """
    # Try local lookup by city hint first (fast path)
    if city_hint:
        key = city_hint.lower().strip()
        guess = CITY_IRRADIANCE.get(key)
        if guess is None:
            for k, v in CITY_IRRADIANCE.items():
                if key in k or k in key:
                    guess = v
                    break
        if guess:
            return guess

    # Try PVGIS API (live)
    try:
        async with httpx.AsyncClient(timeout=8.0) as client:
            resp = await client.get(PVGIS_URL, params={
                "lat": lat, "lon": lng,
                "peakpower": 1, "loss": 14,
                "outputformat": "json",
                "raddatabase": "PVGIS-SARAH2",
                "pvtechtechnology": "crystSi"
            })
            resp.raise_for_status()
            data = resp.json()
            # Extract annual irradiance → convert to daily average
            annual_irr = data["outputs"]["totals"]["fixed"]["H(i)_y"]  # kWh/m²/year
            return round(annual_irr / 365.0, 2)
    except Exception:
        # Fallback: rough lat-based estimate for India
        if 8 <= lat <= 37:
            return round(4.8 + ((lat - 8) / 29.0) * 1.2, 2)
        return 5.5
