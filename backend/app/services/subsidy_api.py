"""
PM Surya Ghar Muft Bijli Yojana — subsidy calculator (2024 policy).

Slabs:
  ≤ 1 kW  → ₹30,000/kW  (max ₹30,000)
  1–2 kW  → ₹30,000 + ₹18,000/kW for the incremental kW above 1 kW
  > 2 kW  → ₹78,000 (fixed cap)

References:
  https://pmsuryaghar.gov.in/
  MNRE OM dated 29-Feb-2024
"""


def calculate_subsidy(capacity_kw: float) -> int:
    """Return total subsidy amount in INR for given system capacity."""
    if capacity_kw <= 1.0:
        return int(30_000 * min(capacity_kw, 1.0))
    elif capacity_kw <= 2.0:
        incremental = capacity_kw - 1.0
        return int(30_000 + 18_000 * incremental)
    else:
        return 78_000  # fixed cap above 2 kW


def get_subsidy_breakdown(capacity_kw: float) -> dict:
    """Return a full subsidy breakdown dict for the given capacity."""
    up_to_1kw = min(capacity_kw, 1.0) * 30_000
    one_to_2kw = min(max(capacity_kw - 1.0, 0.0), 1.0) * 18_000
    above_2kw = 0.0 if capacity_kw <= 2.0 else max(0.0, 78_000 - up_to_1kw - one_to_2kw)
    total = calculate_subsidy(capacity_kw)

    return {
        "up_to_1kw": int(up_to_1kw),
        "one_to_2kw": int(one_to_2kw),
        "above_2kw": int(above_2kw),
        "total_subsidy": total,
        "scheme_name": "PM Surya Ghar Muft Bijli Yojana",
    }


def get_slab_label(capacity_kw: float) -> str:
    if capacity_kw <= 1.0:
        return f"Up to 1 kW — ₹{int(30_000 * capacity_kw):,}"
    elif capacity_kw <= 2.0:
        return f"1–2 kW — ₹{calculate_subsidy(capacity_kw):,}"
    else:
        return "Above 2 kW — ₹78,000 (maximum cap)"
