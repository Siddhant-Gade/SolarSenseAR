"""
SolarSense AR — Shadow & Sun Path Analysis
Run: python shadow_analysis.py
Outputs:
  - sun_altitude_heatmap.png  (month × hour heat map for all cities)
  - monthly_solar_hours.png   (effective generation hours per month)
  - shadow_loss_table.csv     (obstacle shadow loss by hour)
"""
import ephem
import numpy as np
import datetime
import csv
import math

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

CITIES = {
    "Nagpur":     ("21.15", "79.09"),
    "Jaipur":     ("26.91", "75.79"),
    "Mumbai":     ("19.07", "72.87"),
    "Chennai":    ("13.08", "80.27"),
    "Bengaluru":  ("12.97", "77.59"),
    "Kolkata":    ("22.57", "88.37"),
    "Ahmedabad":  ("23.02", "72.57"),
}
MONTH_NAMES = ["Jan","Feb","Mar","Apr","May","Jun",
               "Jul","Aug","Sep","Oct","Nov","Dec"]

# ─────────────────────────────────────────────
# 1. SUN PATH COMPUTATION
# ─────────────────────────────────────────────

def compute_sun_path(lat: str, lng: str, year: int = 2025) -> dict:
    """Return {month: [(hour, altitude_deg, azimuth_deg), ...]} for mid-month days."""
    observer = ephem.Observer()
    observer.lat = lat
    observer.lon = lng
    observer.pressure = 0   # ignore atmospheric refraction for clean values

    sun = ephem.Sun()
    result = {}
    for month in range(1, 13):
        hourly = []
        for hour in range(5, 20):   # 5 AM → 7 PM IST window
            observer.date = datetime.datetime(year, month, 15, hour - 5, 30)
            sun.compute(observer)
            alt_deg = float(sun.alt) * 180.0 / math.pi
            az_deg  = float(sun.az)  * 180.0 / math.pi
            if alt_deg > 0:
                hourly.append((hour, round(alt_deg, 1), round(az_deg, 1)))
        result[month] = hourly
    return result

def effective_hours(sun_path: dict, min_alt: float = 10.0) -> list[int]:
    return [
        sum(1 for _, alt, _ in entries if alt > min_alt)
        for entries in sun_path.values()
    ]

print("=" * 55)
print("  SolarSense AR — Shadow & Sun Path Analysis")
print("=" * 55)
print(f"\n[1/4] Computing sun paths for {len(CITIES)} Indian cities...")

all_paths = {}
for city, coords in CITIES.items():
    all_paths[city] = compute_sun_path(*coords)
    peak = max(
        (alt for entries in all_paths[city].values() for _, alt, _ in entries),
        default=0
    )
    print(f"      {city:12s} — peak altitude: {peak:.1f}°")

# ─────────────────────────────────────────────
# 2. SUN ALTITUDE HEAT MAP (Nagpur — main city)
# ─────────────────────────────────────────────
print("\n[2/4] Generating sun altitude heat map (Nagpur)...")

nagpur = all_paths["Nagpur"]
hours_range = list(range(5, 20))
heat = np.zeros((12, len(hours_range)))

for m_idx, month in enumerate(range(1, 13)):
    for h, alt, _ in nagpur[month]:
        if h in hours_range:
            heat[m_idx, h - 5] = alt

fig, ax = plt.subplots(figsize=(14, 5))
im = ax.imshow(heat, aspect="auto", cmap="YlOrRd", origin="upper",
               vmin=0, vmax=90)
ax.set_xticks(range(len(hours_range)))
ax.set_xticklabels([f"{h}:00" for h in hours_range], rotation=45, fontsize=9)
ax.set_yticks(range(12))
ax.set_yticklabels(MONTH_NAMES, fontsize=9)
ax.set_title("Sun Altitude (°) — Nagpur | Month × Hour Heat Map", fontsize=13, pad=12)
cbar = plt.colorbar(im, ax=ax, label="Solar Altitude (°)")

# Annotate peak cells
for m in range(12):
    for h in range(len(hours_range)):
        val = heat[m, h]
        if val > 0:
            ax.text(h, m, f"{val:.0f}", ha="center", va="center",
                    fontsize=6, color="black" if val < 60 else "white")

plt.tight_layout()
plt.savefig("sun_altitude_heatmap.png", dpi=150)
print("      Saved → sun_altitude_heatmap.png")

# ─────────────────────────────────────────────
# 3. MONTHLY EFFECTIVE HOURS — ALL CITIES
# ─────────────────────────────────────────────
print("\n[3/4] Plotting monthly effective solar hours (all cities)...")

fig, ax = plt.subplots(figsize=(12, 5))
CITY_COLORS = ["#F5A623","#1B2A4A","#22C55E","#EF4444","#3B82F6","#A855F7","#F97316"]

for (city, path), color in zip(all_paths.items(), CITY_COLORS):
    eff = effective_hours(path, min_alt=10.0)
    ax.plot(MONTH_NAMES, eff, marker="o", label=city, color=color,
            linewidth=2, markersize=5)

ax.set_ylabel("Effective Generation Hours/Day", fontsize=11)
ax.set_title("Monthly Effective Solar Hours (altitude > 10°) — Indian Cities", fontsize=13, pad=12)
ax.legend(fontsize=9, loc="lower left")
ax.set_ylim(0, 14)
ax.grid(axis="y", alpha=0.3)
plt.tight_layout()
plt.savefig("monthly_solar_hours.png", dpi=150)
print("      Saved → monthly_solar_hours.png")

# ─────────────────────────────────────────────
# 4. SHADOW LOSS TABLE — OBSTACLE SIMULATION
# ─────────────────────────────────────────────
print("\n[4/4] Computing shadow loss table (AC unit obstacle)...")

def shadow_loss_pct(obstacle_h: float, distance: float, sun_alt_deg: float) -> float:
    """Fraction of 1.75m-tall solar panel in shadow from an obstacle."""
    if sun_alt_deg <= 0:
        return 0.0
    shadow_len = obstacle_h / math.tan(math.radians(max(sun_alt_deg, 0.5)))
    if shadow_len <= distance:
        return 0.0
    overlap = min(shadow_len - distance, 1.75)
    return round(overlap / 1.75 * 100, 1)

# Simulate for mid-December (worst shadow day)
dec_path = [(h, alt) for h, alt, _ in nagpur[12]]

rows = []
for h, alt in dec_path:
    loss_1m = shadow_loss_pct(1.0, 2.0, alt)   # 1m obstacle, 2m away
    loss_2m = shadow_loss_pct(2.0, 3.0, alt)   # 2m obstacle, 3m away (water tank)
    rows.append({
        "Hour": f"{h}:00",
        "Sun_Alt_deg": alt,
        "Shadow_1m_obj_@2m_%": loss_1m,
        "Shadow_2m_obj_@3m_%": loss_2m,
    })

CSV_PATH = "shadow_loss_table.csv"
with open(CSV_PATH, "w", newline="") as f:
    writer = csv.DictWriter(f, fieldnames=rows[0].keys())
    writer.writeheader()
    writer.writerows(rows)
print(f"      Saved → {CSV_PATH}")

# Print summary table
print(f"\n      {'Hour':6} {'Sun Alt°':9} {'1m@2m loss%':12} {'2m@3m loss%'}")
print(f"      {'─'*45}")
for r in rows:
    print(f"      {r['Hour']:6} {r['Sun_Alt_deg']:9.1f} "
          f"{r['Shadow_1m_obj_@2m_%']:12} {r['Shadow_2m_obj_@3m_%']:.1f}%")

# Annual effective hours summary
print(f"\n── Annual Effective Solar Hours ───────────────────")
for city, path in all_paths.items():
    annual = sum(effective_hours(path, min_alt=10.0))
    print(f"   {city:12s}: {annual:4d} hrs/year")

print("\n✓ All done!\n")
