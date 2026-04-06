"""
SolarSense AR — Subsidy & ROI Calculator (Local Training / Batch Analysis)
Run: python subsidy_roi_analysis.py
Outputs:
  - roi_curves.png         (payback curves for different city + panel configs)
  - subsidy_vs_size.png    (PM Surya Ghar subsidy slab visualisation)
  - city_comparison.csv    (tabular ROI summary for 13 cities × 3 system sizes)
"""
import numpy as np
import pandas as pd
import math

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

# ─────────────────────────────────────────────
# CONSTANTS (2024 policy)
# ─────────────────────────────────────────────
PANEL_WATT     = 550           # W per panel (Tier-1 mono-PERC)
COST_PER_KW    = 60_000        # INR / kW installed
GRID_RATE      = 8.0           # INR / unit (kWh)
PR             = 0.80          # Performance Ratio
INFLATION_RATE = 0.06          # Annual electricity tariff inflation

# PVGIS-aligned daily irradiance (kWh/m²/day)
CITY_IRRADIANCE: dict[str, float] = {
    "Nagpur":     5.80, "Jaipur":     5.97, "Ahmedabad":  5.99,
    "Mumbai":     5.52, "Pune":       5.55, "Delhi":      5.61,
    "Bangalore":  5.83, "Hyderabad":  5.82, "Chennai":    5.94,
    "Kolkata":    5.00, "Lucknow":    5.40, "Bhopal":     5.65,
    "Kochi":      5.10,
}

MONTHLY_FACTORS = [0.82, 0.90, 1.05, 1.15, 1.18, 1.10,
                   0.98, 0.95, 1.00, 1.08, 0.90, 0.79]

# ─────────────────────────────────────────────
# PM SURYA GHAR SUBSIDY (3-slab, Feb 2024)
# ─────────────────────────────────────────────
def subsidy(kw: float) -> int:
    if kw <= 1.0:
        return int(30_000 * kw)
    elif kw <= 2.0:
        return int(30_000 + 18_000 * (kw - 1.0))
    else:
        return 78_000


def monthly_breakdown(annual_kwh: float) -> list[int]:
    monthly_avg = annual_kwh / 12.0
    return [int(monthly_avg * f) for f in MONTHLY_FACTORS]


def compute_roi(panels: int, city: str, shadow_pct: float = 5.0,
                roof_type: str = "flat") -> dict:
    kw         = panels * PANEL_WATT / 1000.0
    irr        = CITY_IRRADIANCE.get(city, 5.5)
    tilt_f     = 1.05 if roof_type == "sloped" else 1.0
    shadow_f   = 1 - shadow_pct / 100.0

    annual_kwh = kw * irr * 365 * PR * tilt_f * shadow_f
    monthly_kwh = int(annual_kwh / 12)

    gross       = int(kw * COST_PER_KW)
    sub         = subsidy(kw)
    net         = gross - sub
    annual_sav  = int(annual_kwh * GRID_RATE)
    payback     = round(net / annual_sav, 1) if annual_sav else 999
    sav_25yr    = int(annual_sav * 25 - net)       # simple (no inflation)

    # With tariff inflation compounding
    sav_inf = sum(annual_kwh * GRID_RATE * (1 + INFLATION_RATE) ** y
                  for y in range(25))
    roi_inf = int(sav_inf - net)

    co2 = int(annual_kwh * 0.0816)    # kg CO₂ saved per year
    trees = int(annual_kwh / 22.0)

    return {
        "city": city, "panels": panels, "capacity_kw": round(kw, 2),
        "irradiance": irr, "annual_kwh": int(annual_kwh),
        "monthly_kwh": monthly_kwh,
        "gross_cost": gross, "subsidy": sub, "net_cost": net,
        "annual_savings": annual_sav, "payback_years": payback,
        "savings_25yr": sav_25yr, "savings_25yr_with_inflation": roi_inf,
        "co2_kg_yr": co2, "trees": trees,
    }


print("=" * 55)
print("  SolarSense AR — Subsidy & ROI Analysis")
print("=" * 55)

# ─────────────────────────────────────────────
# 1. CITY × SYSTEM SIZE MATRIX
# ─────────────────────────────────────────────
print("\n[1/4] Computing ROI matrix (13 cities × 3 sizes)...")

SIZES = [6, 12, 20]   # panel counts
rows = []
for city in CITY_IRRADIANCE:
    for p in SIZES:
        rows.append(compute_roi(p, city))

df = pd.DataFrame(rows)

CSV_PATH = "city_comparison.csv"
df.to_csv(CSV_PATH, index=False)
print(f"      Saved → {CSV_PATH}")

# Terminal summary
print(f"\n      {'City':12} {'kW':5} {'Net Cost':10} {'Payback':9} {'25yr Savings'}")
print(f"      {'─'*58}")
for _, r in df[df.panels == 12].iterrows():
    print(f"      {r.city:12} {r.capacity_kw:5.1f} "
          f"₹{r.net_cost/1e5:6.2f}L   {r.payback_years:5.1f} yrs "
          f"₹{r.savings_25yr/1e5:.1f}L")

# ─────────────────────────────────────────────
# 2. SUBSIDY SLAB VISUALISATION
# ─────────────────────────────────────────────
print("\n[2/4] Plotting PM Surya Ghar subsidy curve...")

kw_range = np.linspace(0.5, 5.0, 200)
subs = [subsidy(k) for k in kw_range]
gross = [k * COST_PER_KW for k in kw_range]
net   = [g - s for g, s in zip(gross, subs)]

fig, ax = plt.subplots(figsize=(10, 5))
ax.fill_between(kw_range, subs, alpha=0.25, color="#22C55E", label="Subsidy")
ax.plot(kw_range, gross, "--", color="#9CA3AF", lw=1.5, label="Gross Cost")
ax.plot(kw_range, net,   "-",  color="#1B2A4A", lw=2,   label="Net Cost (after subsidy)")
ax.plot(kw_range, subs,  "-",  color="#22C55E", lw=2,   label="PM Surya Ghar Subsidy")

# Slab boundaries
for x, label in [(1.0, "₹30K/kW"), (2.0, "₹18K/kW\n(+₹30K base)"), (3.0, "₹78K cap")]:
    if x <= kw_range[-1]:
        ax.axvline(x, color="#F5A623", ls=":", lw=1.5)
        ax.text(x + 0.04, max(subs) * 0.95, label, color="#F5A623", fontsize=8)

ax.set_xlabel("System Capacity (kW)", fontsize=11)
ax.set_ylabel("Amount (INR)", fontsize=11)
ax.yaxis.set_major_formatter(plt.FuncFormatter(lambda v, _: f"₹{v/1000:.0f}K"))
ax.set_title("PM Surya Ghar Muft Bijli Yojana — Subsidy vs System Size", fontsize=13, pad=12)
ax.legend(fontsize=9)
ax.grid(axis="y", alpha=0.25)
plt.tight_layout()
plt.savefig("subsidy_vs_size.png", dpi=150)
print("      Saved → subsidy_vs_size.png")

# ─────────────────────────────────────────────
# 3. ROI CURVES — TOP CITIES
# ─────────────────────────────────────────────
print("\n[3/4] Plotting 25-year ROI curves...")

TOP_CITIES = ["Nagpur", "Jaipur", "Chennai", "Bangalore", "Kolkata"]
PANELS     = 12
COLORS     = ["#F5A623", "#1B2A4A", "#22C55E", "#3B82F6", "#EF4444"]

fig, ax = plt.subplots(figsize=(12, 6))
for city, color in zip(TOP_CITIES, COLORS):
    r = compute_roi(PANELS, city)
    net = r["net_cost"]
    sav = r["annual_savings"]
    # Cumulative profit year by year with inflation
    cum = [sum(r["annual_kwh"] * GRID_RATE * (1 + INFLATION_RATE) ** y
               for y in range(yr)) - net
           for yr in range(26)]
    ax.plot(range(26), [c / 1e5 for c in cum],
            label=f"{city} (payback {r['payback_years']}yr)", color=color, lw=2)

ax.axhline(0, color="gray", ls="--", lw=1)
ax.axvline(7, color="#F5A623", ls=":", lw=1, alpha=0.6)
ax.text(7.1, ax.get_ylim()[0] * 0.05 if ax.get_ylim()[0] < 0 else 0.5,
        "~7yr avg payback", color="#F5A623", fontsize=8)
ax.set_xlabel("Year", fontsize=11)
ax.set_ylabel("Cumulative Profit (₹ Lakh)", fontsize=11)
ax.set_title(f"25-Year ROI — 12-Panel (6.6 kW) System | Top Indian Cities", fontsize=13, pad=12)
ax.legend(fontsize=9, loc="upper left")
ax.grid(alpha=0.2)
plt.tight_layout()
plt.savefig("roi_curves.png", dpi=150)
print("      Saved → roi_curves.png")

# ─────────────────────────────────────────────
# 4. MONTHLY GENERATION FORECAST (sample)
# ─────────────────────────────────────────────
print("\n[4/4] Monthly generation breakdown (Nagpur, 12 panels)...")

r      = compute_roi(12, "Nagpur")
months_kwh = monthly_breakdown(r["annual_kwh"])
MONTH_NAMES = ["Jan","Feb","Mar","Apr","May","Jun",
               "Jul","Aug","Sep","Oct","Nov","Dec"]

fig, ax = plt.subplots(figsize=(11, 4))
bars = ax.bar(MONTH_NAMES, months_kwh, color="#F5A623", edgecolor="white", width=0.7)
for bar, val in zip(bars, months_kwh):
    ax.text(bar.get_x() + bar.get_width() / 2, val + 3, str(val),
            ha="center", fontsize=8.5, color="#1B2A4A", fontweight="bold")
ax.set_ylabel("Generation (kWh)", fontsize=11)
ax.set_title("Monthly Solar Generation — 12 Panels, 6.6 kW | Nagpur", fontsize=13, pad=12)
ax.grid(axis="y", alpha=0.2)
plt.tight_layout()
plt.savefig("monthly_generation.png", dpi=150)
print("      Saved → monthly_generation.png")

print(f"\n── Nagpur 12-panel Summary ────────────────────────")
print(f"   Gross Cost    : ₹{r['gross_cost']:,}")
print(f"   Subsidy       : ₹{r['subsidy']:,}")
print(f"   Net Cost      : ₹{r['net_cost']:,}")
print(f"   Annual Savings: ₹{r['annual_savings']:,}")
print(f"   Payback       : {r['payback_years']} years")
print(f"   25yr Savings  : ₹{r['savings_25yr']:,}")
print(f"   CO₂ Saved     : {r['co2_kg_yr']} kg/year")
print(f"   Trees Equiv.  : {r['trees']}")

print("\n✓ All done!\n")
