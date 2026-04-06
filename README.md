# SolarSense AR — Full Stack Mobile App

## Project Structure

```
SolarSenseAR/
├── app/                          ← Android app (Kotlin + Jetpack Compose)
│   └── src/main/java/com/solarsensear/
│       ├── MainActivity.kt
│       ├── ar/                   ← ARSceneManager, PanelRenderer, ShadowOverlay
│       ├── data/
│       │   ├── mock/             ← MockData
│       │   └── models/           ← SolarReport, Vendor, UserProfile
│       ├── domain/               ← LocalSolarCalculator, SubsidyCalculator, etc.
│       ├── network/              ← RetrofitClient, ApiService
│       └── ui/
│           ├── components/       ← Charts, Buttons, Cards, VendorCard
│           ├── navigation/       ← NavGraph, Screen
│           ├── screens/
│           │   ├── ar/           ← ARScreen, SetupSheet, AnalyzeViewModel
│           │   ├── calculator/   ← CalculatorScreen
│           │   ├── home/         ← HomeScreen
│           │   ├── login/        ← LoginScreen
│           │   ├── onboarding/   ← OnboardingScreen
│           │   ├── profile/      ← ProfileScreen
│           │   ├── report/       ← ReportScreen (4 tabs)
│           │   ├── reports/      ← ReportsListScreen
│           │   ├── splash/       ← SplashScreen
│           │   └── vendors/      ← VendorsScreen
│           └── theme/            ← Color, Type, Shape, Theme
│
├── backend/                      ← FastAPI Python backend
│   ├── main.py
│   ├── requirements.txt
│   ├── Dockerfile
│   ├── .env.example
│   └── app/
│       ├── core/                 ← config, database, security
│       ├── models/               ← db_models (SQLAlchemy), schemas (Pydantic)
│       ├── routers/              ← auth, analyze, vendors, report
│       └── services/             ← pvgis_client, subsidy_api, pdf_builder
│
└── ml-training/                  ← Jupyter notebooks
    ├── energy_regression.ipynb
    ├── shadow_analysis_demo.ipynb
    └── yolo_obstacle_train.ipynb
```

## Quick Start — Android App

```powershell
# Open in Android Studio
# Requires: Android Studio Hedgehog+, JDK 17, Android SDK 34
# ARCore requires physical ARCore-supported device to test AR
cd SolarSenseAR
./gradlew assembleDebug
```

## Quick Start — Backend

```powershell
cd backend

# Create virtual environment
python -m venv venv
.\venv\Scripts\Activate.ps1

# Install dependencies
pip install -r requirements.txt

# Copy and configure environment
Copy-Item .env.example .env
# Edit .env: set DATABASE_URL, SECRET_KEY

# Run (development)
uvicorn main:app --reload --port 8000

# Docs available at: http://localhost:8000/docs
```

## Quick Start — ML Training

```powershell
cd ml-training
pip install jupyter scikit-learn numpy pandas matplotlib skl2onnx onnx ephem ultralytics

# Open any notebook
jupyter notebook energy_regression.ipynb
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register new user |
| POST | `/api/v1/auth/login` | Login + get JWT |
| POST | `/api/v1/analyze` | Run AI analysis pipeline |
| GET  | `/api/v1/report/{id}` | Get scan summary |
| GET  | `/api/v1/report/{id}/pdf` | Download PDF report |
| GET  | `/api/v1/user/scans` | List scan history |
| GET  | `/api/v1/vendors?lat=&lng=` | Nearby solar installers |

## Key Features

- **Real ARCore integration** — plane detection, solar panel overlay
- **Live calculator** — `derivedStateOf` real-time updates as user moves sliders  
- **4-tab report dashboard** — Summary, Financials, Energy, Actions
- **Canvas-based charts** — animated bar chart (Jan–Dec), 25yr line chart, donut, progress bar
- **PM Surya Ghar subsidy** — correct 3-slab policy (≤1kW, 1–2kW, >2kW)
- **PDF generation** — ReportLab A4 report with branded tables
- **PVGIS irradiance** — live API with city lookup fallback for 20 Indian cities

## PM Surya Ghar Subsidy Slabs (2024)

| System Size | Subsidy |
|-------------|---------|
| ≤ 1 kW | ₹30,000/kW |
| 1–2 kW | ₹30,000 + ₹18,000/kW for incremental kW |
| > 2 kW | ₹78,000 (fixed maximum cap) |
