"""
PDF report generator using ReportLab.
Produces a professional solar assessment report in A4 format.
"""
from io import BytesIO
from datetime import datetime

from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import mm
from reportlab.platypus import (
    BaseDocTemplate, Frame, PageTemplate, Paragraph,
    Spacer, Table, TableStyle, HRFlowable
)
from reportlab.lib.enums import TA_CENTER, TA_LEFT


BRAND_NAVY = colors.HexColor("#1B2A4A")
BRAND_AMBER = colors.HexColor("#F5A623")
BRAND_GREEN = colors.HexColor("#22C55E")
BRAND_LIGHT = colors.HexColor("#F9FAFB")


def generate_report_pdf(scan_data: dict) -> bytes:
    """
    Generate a PDF for the given scan data dict.
    Keys expected: location_name, panel_count, system_kw, monthly_gen_units,
    annual_gen_units, gross_cost_inr, subsidy_inr, net_cost_inr,
    annual_savings_inr, payback_years, savings_25yr_inr, co2_kg_annual,
    irradiance_kwh_m2_day, ai_narrative, created_at (datetime or str)
    """
    buf = BytesIO()
    doc = BaseDocTemplate(buf, pagesize=A4, leftMargin=20*mm, rightMargin=20*mm,
                          topMargin=20*mm, bottomMargin=20*mm)

    frame = Frame(doc.leftMargin, doc.bottomMargin,
                  doc.width, doc.height, id="main")
    doc.addPageTemplates([PageTemplate(id="main", frames=frame,
                                       onPage=_header_footer)])

    styles = getSampleStyleSheet()
    h1 = ParagraphStyle("H1", parent=styles["Heading1"], fontSize=22, textColor=BRAND_NAVY,
                         spaceAfter=4, leading=28)
    h2 = ParagraphStyle("H2", parent=styles["Heading2"], fontSize=14, textColor=BRAND_NAVY,
                         spaceBefore=12, spaceAfter=4)
    body = ParagraphStyle("Body", parent=styles["Normal"], fontSize=10, leading=15,
                           textColor=colors.HexColor("#374151"))
    badge = ParagraphStyle("Badge", parent=styles["Normal"], fontSize=9,
                            textColor=colors.white, backColor=BRAND_GREEN,
                            borderPadding=(3, 8, 3, 8), alignment=TA_CENTER)

    story = []

    # ── Title block ──────────────────────────────────────────────────────────
    story.append(Paragraph("☀ SolarSense AR — Solar Assessment Report", h1))
    story.append(Paragraph(
        f"<font color='#6B7280'>Generated: {datetime.now().strftime('%d %b %Y, %I:%M %p')} | "
        f"Location: {scan_data.get('location_name', 'N/A')}</font>",
        body
    ))
    story.append(HRFlowable(width="100%", thickness=2, color=BRAND_AMBER, spaceAfter=12))

    # ── Hero savings ─────────────────────────────────────────────────────────
    savings_25yr = scan_data.get("savings_25yr_inr", 0)
    story.append(Paragraph("25-Year Total Savings", h2))
    story.append(Table(
        [[f"₹{savings_25yr:,}"]],
        colWidths=[doc.width],
        style=TableStyle([
            ("BACKGROUND", (0, 0), (-1, -1), BRAND_NAVY),
            ("TEXTCOLOR", (0, 0), (-1, -1), BRAND_AMBER),
            ("FONTSIZE", (0, 0), (-1, -1), 28),
            ("FONTNAME", (0, 0), (-1, -1), "Helvetica-Bold"),
            ("ALIGN", (0, 0), (-1, -1), "CENTER"),
            ("TOPPADDING", (0, 0), (-1, -1), 16),
            ("BOTTOMPADDING", (0, 0), (-1, -1), 16),
            ("ROUNDEDCORNERS", [6]),
        ])
    ))
    story.append(Spacer(1, 12))

    # ── System overview table ─────────────────────────────────────────────────
    story.append(Paragraph("System Overview", h2))
    overview = [
        ["Parameter", "Value"],
        ["Panel Count", f"{scan_data.get('panel_count', 0)} panels"],
        ["System Capacity", f"{scan_data.get('system_kw', 0):.1f} kW"],
        ["Monthly Generation", f"{scan_data.get('monthly_gen_units', 0)} kWh"],
        ["Annual Generation", f"{scan_data.get('annual_gen_units', 0)} kWh"],
        ["Solar Irradiance", f"{scan_data.get('irradiance_kwh_m2_day', 5.5):.1f} kWh/m²/day"],
        ["CO₂ Offset", f"{scan_data.get('co2_kg_annual', 0)} kg/year"],
    ]
    story.append(_make_table(overview, doc.width))
    story.append(Spacer(1, 12))

    # ── Financial breakdown ───────────────────────────────────────────────────
    story.append(Paragraph("Financial Breakdown", h2))
    financials = [
        ["Item", "Amount (INR)"],
        ["Gross Installation Cost", f"₹{scan_data.get('gross_cost_inr', 0):,}"],
        ["PM Surya Ghar Subsidy", f"− ₹{scan_data.get('subsidy_inr', 0):,}"],
        ["Net Investment", f"₹{scan_data.get('net_cost_inr', 0):,}"],
        ["Annual Electricity Savings", f"₹{scan_data.get('annual_savings_inr', 0):,}"],
        ["Payback Period", f"{scan_data.get('payback_years', 0):.1f} years"],
        ["25-Year Net Profit", f"₹{savings_25yr:,}"],
    ]
    story.append(_make_table(financials, doc.width, highlight_last=True))
    story.append(Spacer(1, 12))

    # ── Subsidy badge ─────────────────────────────────────────────────────────
    story.append(Paragraph(
        "✓  PM Surya Ghar Muft Bijli Yojana subsidy has been applied to this estimate.",
        ParagraphStyle("GreenNote", parent=body, textColor=BRAND_GREEN,
                       backColor=colors.HexColor("#DCFCE7"),
                       borderPadding=(6, 10, 6, 10))
    ))
    story.append(Spacer(1, 12))

    # ── AI Analysis ───────────────────────────────────────────────────────────
    narrative = scan_data.get("ai_narrative", "")
    if narrative:
        story.append(Paragraph("AI Analysis", h2))
        story.append(Paragraph(narrative, body))
        story.append(Spacer(1, 8))

    # ── Footer note ───────────────────────────────────────────────────────────
    story.append(HRFlowable(width="100%", thickness=1, color=BRAND_AMBER, spaceAfter=8))
    story.append(Paragraph(
        "Estimates are based on PVGIS irradiance data and standard performance ratios. "
        "Actual savings may vary ±10% based on site conditions. "
        "Apply for PM Surya Ghar subsidy at: pmsuryaghar.gov.in",
        ParagraphStyle("Footer", parent=body, fontSize=9, textColor=colors.HexColor("#9CA3AF"))
    ))

    doc.build(story)
    return buf.getvalue()


def _make_table(data: list[list], width: float, highlight_last: bool = False) -> Table:
    col_w = [width * 0.55, width * 0.45]
    tbl = Table(data, colWidths=col_w)
    style = [
        ("BACKGROUND", (0, 0), (-1, 0), BRAND_NAVY),
        ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
        ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),
        ("FONTSIZE", (0, 0), (-1, -1), 10),
        ("ALIGN", (1, 0), (1, -1), "RIGHT"),
        ("ROWBACKGROUNDS", (0, 1), (-1, -1), [BRAND_LIGHT, colors.white]),
        ("GRID", (0, 0), (-1, -1), 0.5, colors.HexColor("#E5E7EB")),
        ("TOPPADDING", (0, 0), (-1, -1), 7),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 7),
        ("LEFTPADDING", (0, 0), (-1, -1), 10),
    ]
    if highlight_last:
        style += [
            ("BACKGROUND", (0, -1), (-1, -1), BRAND_AMBER),
            ("TEXTCOLOR", (0, -1), (-1, -1), BRAND_NAVY),
            ("FONTNAME", (0, -1), (-1, -1), "Helvetica-Bold"),
        ]
    tbl.setStyle(TableStyle(style))
    return tbl


def _header_footer(canvas, doc):
    canvas.saveState()
    canvas.setFont("Helvetica", 8)
    canvas.setFillColor(colors.HexColor("#9CA3AF"))
    canvas.drawString(20*mm, 10*mm, "SolarSense AR  |  Confidential Solar Assessment")
    canvas.drawRightString(A4[0] - 20*mm, 10*mm, f"Page {canvas.getPageNumber()}")
    canvas.restoreState()
