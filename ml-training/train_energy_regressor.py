"""
SolarSense AR — Energy Regression Trainer
Run: python train_energy_regressor.py
Outputs:
  - energy_regressor.pkl    (scikit-learn pipeline)
  - feature_importances.png
  - predictions_scatter.png
"""
import numpy as np
import pandas as pd
import matplotlib
matplotlib.use("Agg")  # headless — no display required
import matplotlib.pyplot as plt
import pickle
import os
from sklearn.ensemble import GradientBoostingRegressor
from sklearn.model_selection import train_test_split, cross_val_score
from sklearn.preprocessing import StandardScaler
from sklearn.pipeline import Pipeline
from sklearn.metrics import mean_absolute_error, r2_score

# ─────────────────────────────────────────────
# 1. GENERATE SYNTHETIC DATASET
# ─────────────────────────────────────────────
print("=" * 55)
print("  SolarSense AR — Energy Regression Trainer")
print("=" * 55)

np.random.seed(42)
N = 8000

panel_count   = np.random.randint(2, 50, N)
capacity_kw   = panel_count * 0.55
irradiance    = np.random.uniform(4.5, 6.5, N)      # kWh/m²/day
tilt_deg      = np.random.choice([0, 15, 25, 30, 45], N)
shadow_pct    = np.random.uniform(0, 20, N)          # % shading loss
perf_ratio    = np.random.uniform(0.70, 0.85, N)
is_sloped     = (tilt_deg > 0).astype(float)
month         = np.random.randint(1, 13, N)

# Monthly generation factor (higher in summer / spring)
MONTH_FACTORS = [0.82, 0.90, 1.05, 1.15, 1.18, 1.10,
                 0.98, 0.95, 1.00, 1.08, 0.90, 0.79]
month_factor  = np.array([MONTH_FACTORS[m - 1] for m in month])

tilt_factor   = 1 + 0.05 * is_sloped
monthly_kwh   = (
    capacity_kw * irradiance * 30 * perf_ratio
    * tilt_factor * (1 - shadow_pct / 100) * month_factor
    + np.random.normal(0, 12, N)
).clip(0)

FEATURES = ["panel_count", "capacity_kw", "irradiance",
            "tilt_deg", "shadow_pct", "perf_ratio", "is_sloped", "month"]

df = pd.DataFrame({
    "panel_count": panel_count,
    "capacity_kw": capacity_kw,
    "irradiance":  irradiance,
    "tilt_deg":    tilt_deg,
    "shadow_pct":  shadow_pct,
    "perf_ratio":  perf_ratio,
    "is_sloped":   is_sloped,
    "month":       month,
    "monthly_kwh": monthly_kwh,
})

print(f"\n[1/5] Dataset generated: {len(df):,} samples")
print(f"      Monthly kWh  — mean: {monthly_kwh.mean():.1f}, "
      f"min: {monthly_kwh.min():.1f}, max: {monthly_kwh.max():.1f}")

# ─────────────────────────────────────────────
# 2. TRAIN / TEST SPLIT
# ─────────────────────────────────────────────
X = df[FEATURES].values
y = df["monthly_kwh"].values

X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42
)
print(f"\n[2/5] Split — Train: {len(X_train):,}  |  Test: {len(X_test):,}")

# ─────────────────────────────────────────────
# 3. TRAIN GRADIENT BOOSTING PIPELINE
# ─────────────────────────────────────────────
print("\n[3/5] Training GradientBoostingRegressor...")

pipeline = Pipeline([
    ("scaler", StandardScaler()),
    ("model", GradientBoostingRegressor(
        n_estimators=300,
        max_depth=4,
        learning_rate=0.08,
        subsample=0.8,
        random_state=42,
        verbose=0,
    )),
])

pipeline.fit(X_train, y_train)
print("      Training complete.")

y_pred = pipeline.predict(X_test)
mae    = mean_absolute_error(y_test, y_pred)
r2     = r2_score(y_test, y_pred)
mape   = np.mean(np.abs((y_test - y_pred) / (y_test + 1e-8))) * 100

print(f"\n      ── Metrics ──────────────────")
print(f"      MAE  : {mae:.2f} kWh")
print(f"      R²   : {r2:.4f}")
print(f"      MAPE : {mape:.2f}%  (target < 10%)")
print(f"      Status: {'✓ PASS' if mape < 10 else '✗ FAIL — tune hyperparams'}")

# ─────────────────────────────────────────────
# 4. CROSS-VALIDATION
# ─────────────────────────────────────────────
print("\n[4/5] Running 5-fold cross-validation...")
cv_scores = cross_val_score(pipeline, X, y, cv=5, scoring="r2")
print(f"      CV R²: {cv_scores.round(4)}")
print(f"      Mean : {cv_scores.mean():.4f} ± {cv_scores.std():.4f}")

# ─────────────────────────────────────────────
# 5. SAVE MODEL + PLOTS
# ─────────────────────────────────────────────
print("\n[5/5] Saving artifacts...")

# --- Model pickle ---
MODEL_PATH = "energy_regressor.pkl"
with open(MODEL_PATH, "wb") as f:
    pickle.dump({"pipeline": pipeline, "features": FEATURES}, f)
print(f"      Model saved → {MODEL_PATH}")

# --- Feature importances ---
importances = pipeline.named_steps["model"].feature_importances_
fig, ax = plt.subplots(figsize=(9, 5))
colors = ["#F5A623" if imp > np.median(importances) else "#1B2A4A"
          for imp in importances]
ax.barh(FEATURES, importances, color=colors, edgecolor="white")
ax.set_xlabel("Feature Importance", fontsize=11)
ax.set_title("SolarSense AR — Energy Regression Feature Importances", fontsize=13, pad=12)
ax.invert_yaxis()
for i, (feat, imp) in enumerate(zip(FEATURES, importances)):
    ax.text(imp + 0.001, i, f"{imp:.3f}", va="center", fontsize=9)
plt.tight_layout()
plt.savefig("feature_importances.png", dpi=150)
print("      Plot saved  → feature_importances.png")

# --- Actual vs Predicted ---
fig, ax = plt.subplots(figsize=(6, 6))
ax.scatter(y_test, y_pred, alpha=0.25, color="#1B2A4A", s=8, label="predictions")
lim = [0, max(y_test.max(), y_pred.max())]
ax.plot(lim, lim, "r--", lw=2, label="perfect fit")
ax.set_xlabel("Actual kWh/month", fontsize=11)
ax.set_ylabel("Predicted kWh/month", fontsize=11)
ax.set_title(f"Energy Regression — R² = {r2:.4f}", fontsize=13, pad=12)
ax.legend()
plt.tight_layout()
plt.savefig("predictions_scatter.png", dpi=150)
print("      Plot saved  → predictions_scatter.png")

# ─────────────────────────────────────────────
# DEMO PREDICTION
# ─────────────────────────────────────────────
print("\n── Demo Prediction ──────────────────────────────")
demo = {
    "panel_count": 12, "capacity_kw": 6.6,
    "irradiance": 5.80, "tilt_deg": 0,
    "shadow_pct": 5.0, "perf_ratio": 0.80,
    "is_sloped": 0, "month": 5,  # May
}
x_demo = np.array([[demo[f] for f in FEATURES]])
pred   = pipeline.predict(x_demo)[0]
print(f"   Input  : 12 panels, 6.6 kW, Nagpur irradiance, 5% shadow, May")
print(f"   Output : {pred:.0f} kWh/month predicted generation")

print("\n✓ All done!\n")
