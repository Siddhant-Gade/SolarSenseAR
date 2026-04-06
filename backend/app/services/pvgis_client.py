"""
PVGIS API client — fetches real solar irradiance for a lat/lng via PVGIS-SARAH2.
Endpoint: https://re.jrc.ec.europa.eu/api/v5_2/PVcalc
Falls back to a city lookup table, then a lat-based India estimate.
"""
import httpx

# Fallback lookup table (avg daily irradiance kWh/m²/day, PVGIS-validated values)
CITY_IRRADIANCE: dict[str, float] = {
    "nagpur": 5.80, "mumbai": 5.52, "delhi": 5.61, "bangalore": 5.83,
    "bengaluru": 5.83, "hyderabad": 5.82, "chennai": 5.94, "kolkata": 5.00,
    "pune": 5.55, "ahmedabad": 5.99, "jaipur": 5.97, "jodhpur": 6.10,
    "lucknow": 5.40, "bhopal": 5.65, "surat": 5.70, "indore": 5.69,
    "patna": 5.30, "chandigarh": 5.20, "coimbatore": 5.88, "kochi": 5.10,
    "vizag": 5.70, "visakhapatnam": 5.70, "bhubaneswar": 5.40,
    "raipur": 5.55, "dehradun": 4.90, "shimla": 4.50, "kolhapur": 5.60,
    "aurangabad": 5.62, "nashik": 5.58, "amritsar": 5.10,
}

PVGIS_URL = "https://re.jrc.ec.europa.eu/api/v5_2/PVcalc"


async def get_irradiance(lat: float, lng: float, city_hint: str = "") -> float:
    """
    Returns average daily solar irradiance kWh/m²/day for the given coordinates.
    Strategy:
      1. City lookup (instant, no network)
      2. PVGIS live API (real data, 8s timeout)
      3. Lat-interpolated India estimate (always succeeds)
    """
    # 1. City lookup
    if city_hint:
        key = city_hint.lower().strip()
        irr = CITY_IRRADIANCE.get(key)
        if irr is None:
            for k, v in CITY_IRRADIANCE.items():
                if key in k or k in key:
                    irr = v
                    break
        if irr:
            return irr

    # 2. PVGIS live API
    try:
        async with httpx.AsyncClient(timeout=8.0) as client:
            resp = await client.get(PVGIS_URL, params={
                "lat": lat,
                "lon": lng,
                "peakpower": 1,
                "loss": 14,
                "outputformat": "json",
                "raddatabase": "PVGIS-SARAH2",
                "pvtechtechnology": "crystSi",   # monocrystalline silicon
                "mountingplace": "free",          # free-standing / rooftop
                "optimalangles": 1,               # let PVGIS pick best tilt/azimuth
            })
            resp.raise_for_status()
            data = resp.json()
            # H(i)_y = irradiation on inclined plane (kWh/m²/year)
            annual_irr: float = data["outputs"]["totals"]["fixed"]["H(i)_y"]
            return round(annual_irr / 365.0, 2)
    except Exception:
        pass

    # 3. Lat-based estimate for India (8°N–37°N)
    if 8 <= lat <= 37:
        return round(4.8 + ((lat - 8) / 29.0) * 1.4, 2)
    return 5.5
