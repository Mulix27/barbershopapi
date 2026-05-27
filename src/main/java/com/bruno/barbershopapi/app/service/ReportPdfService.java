package com.bruno.barbershopapi.app.service;

import com.bruno.barbershopapi.app.web.model.report.FullReportResponse;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class ReportPdfService {

    // ── Paleta de colores ─────────────────────────────────────
    private static final BaseColor GOLD       = new BaseColor(201, 168,  76);
    private static final BaseColor DARK_BG    = new BaseColor( 13,  13,  20);
    private static final BaseColor SURFACE    = new BaseColor( 22,  22,  31);
    private static final BaseColor SURFACE2   = new BaseColor( 19,  19,  30);
    private static final BaseColor TEXT_COLOR = new BaseColor(232, 230, 222);
    private static final BaseColor TEXT_MUTED = new BaseColor(150, 148, 140);
    private static final BaseColor SUCCESS    = new BaseColor( 34, 197,  94);
    private static final BaseColor BORDER     = new BaseColor( 40,  38,  28);
    private static final BaseColor BORDER_TH  = new BaseColor( 50,  45,  30);

    // ── Fuentes ───────────────────────────────────────────────
    private static final Font F_SHOP    = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,   GOLD);
    private static final Font F_TITLE   = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD,   TEXT_COLOR);
    private static final Font F_SUB     = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, TEXT_MUTED);
    private static final Font F_SECTION = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD,   GOLD);
    private static final Font F_TH      = new Font(Font.FontFamily.HELVETICA,  9, Font.BOLD,   TEXT_MUTED);
    private static final Font F_TD      = new Font(Font.FontFamily.HELVETICA,  9, Font.NORMAL, TEXT_COLOR);
    private static final Font F_TD_GOLD = new Font(Font.FontFamily.HELVETICA,  9, Font.BOLD,   GOLD);
    private static final Font F_TD_GRN  = new Font(Font.FontFamily.HELVETICA,  9, Font.BOLD,   SUCCESS);
    private static final Font F_SMALL   = new Font(Font.FontFamily.HELVETICA,  8, Font.NORMAL, TEXT_MUTED);
    private static final Font F_MET_V   = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD,   GOLD);
    private static final Font F_MET_L   = new Font(Font.FontFamily.HELVETICA,  8, Font.NORMAL, TEXT_MUTED);

    // ─────────────────────────────────────────────────────────
    //  MÉTODO PRINCIPAL
    // ─────────────────────────────────────────────────────────

    /**
     * Genera el PDF a partir de un FullReportResponse ya calculado.
     * @param report    datos del reporte (viene de ReportService.getFullReport)
     * @param shopName  nombre de la barbería para el encabezado
     * @return          contenido del PDF codificado en Base64
     */
    public String generateBase64(FullReportResponse report, String shopName) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document doc = new Document(PageSize.A4, 40, 40, 50, 50);
        PdfWriter writer = PdfWriter.getInstance(doc, baos);
        writer.setPageEvent(new DarkBackground());
        doc.open();

        // ── Encabezado ────────────────────────────────────────
        buildHeader(doc, shopName, report.periodLabel(), report.from(), report.to());
        addRule(doc, GOLD);
        doc.add(Chunk.NEWLINE);

        // ── Tarjetas de métricas ──────────────────────────────
        buildMetricCards(doc, report);
        doc.add(Chunk.NEWLINE);

        // ── Servicios más vendidos ────────────────────────────
        if (report.topServices() != null && !report.topServices().isEmpty()) {
            addSectionTitle(doc, "Servicios más Rentables");
            buildServicesTable(doc, report);
            doc.add(Chunk.NEWLINE);
        }

        // ── Rendimiento por barbero ───────────────────────────
        if (report.barberPerformance() != null && !report.barberPerformance().isEmpty()) {
            addSectionTitle(doc, "Rendimiento de Barberos");
            buildBarbersTable(doc, report);
            doc.add(Chunk.NEWLINE);
        }

        // ── Métodos de pago ───────────────────────────────────
        if (report.paymentMethods() != null && !report.paymentMethods().isEmpty()) {
            addSectionTitle(doc, "Métodos de Pago");
            buildPaymentTable(doc, report);
            doc.add(Chunk.NEWLINE);
        }

        // ── Métricas de citas ─────────────────────────────────
        if (report.appointmentMetrics() != null) {
            addSectionTitle(doc, "Métricas de Citas");
            buildAppointmentTable(doc, report);
            doc.add(Chunk.NEWLINE);
        }

        // ── Footer ────────────────────────────────────────────
        buildFooter(doc, shopName);

        doc.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    // ─────────────────────────────────────────────────────────
    //  SECCIONES
    // ─────────────────────────────────────────────────────────

    private void buildHeader(Document doc, String shopName,
                             String period, String from, String to) throws DocumentException {
        Paragraph shop = new Paragraph(shopName.toUpperCase(), F_SHOP);
        doc.add(shop);

        Paragraph title = new Paragraph("Informe de Negocios", F_TITLE);
        title.setSpacingBefore(4);
        doc.add(title);

        String range = (from != null && to != null && !from.equals(to))
                ? "  ·  " + from + " — " + to
                : "";
        Paragraph sub = new Paragraph(period + range, F_SUB);
        sub.setSpacingAfter(10);
        doc.add(sub);
    }

    private void buildMetricCards(Document doc, FullReportResponse r) throws DocumentException {
        PdfPTable t = new PdfPTable(4);
        t.setWidthPercentage(100);
        t.setSpacingAfter(6);

        var summary = r.summary();
        double revenue   = summary != null && summary.totalRevenue()    != null ? summary.totalRevenue().doubleValue()    : 0;
        double avgTicket = summary != null && summary.averageTicket()   != null ? summary.averageTicket().doubleValue()   : 0;
        long   sales     = summary != null && summary.totalSales()      != null ? summary.totalSales()                   : 0;

        var am = r.appointmentMetrics();
        long completed   = am != null ? am.completed()           : 0;
        long totalAppts  = am != null ? am.totalAppointments()   : 0;
        double occRate   = totalAppts > 0 ? (double) completed / totalAppts * 100 : 0;

        addMetricCard(t, fmt(revenue),              "Ingresos Totales");
        addMetricCard(t, fmt(revenue * 0.41),       "Ganancia Neta (41%)");
        addMetricCard(t, fmt(avgTicket),            "Ticket Promedio");
        addMetricCard(t, String.format("%.1f%%", occRate), "Tasa de Ocupación");

        doc.add(t);

        // Fila 2 — ventas totales y citas
        PdfPTable t2 = new PdfPTable(4);
        t2.setWidthPercentage(100);
        t2.setSpacingAfter(6);
        addMetricCard(t2, String.valueOf(sales),     "Ventas Registradas");
        addMetricCard(t2, String.valueOf(completed), "Citas Completadas");
        addMetricCard(t2, String.valueOf(totalAppts),"Total Citas");
        addMetricCard(t2, am != null ? String.valueOf(am.noShow()) : "0", "No asistió");
        doc.add(t2);
    }

    private void buildServicesTable(Document doc, FullReportResponse r) throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{3f, 1.2f, 1.8f, 1.8f});
        t.setWidthPercentage(100);

        addTh(t, "Servicio");
        addTh(t, "Cantidad");
        addTh(t, "Total Ventas");
        addTh(t, "Ganancia Neta");

        boolean alt = false;
        for (var svc : r.topServices()) {
            double rev = svc.totalRevenue() != null ? svc.totalRevenue().doubleValue() : 0;
            addTd(t, svc.serviceName() != null ? svc.serviceName() : "—", alt, false);
            addTd(t, String.valueOf(svc.totalQuantity()), alt, false);
            addTd(t, fmt(rev), alt, true);           // dorado
            addTdGreen(t, fmt(rev * 0.82), alt);     // verde
            alt = !alt;
        }

        // Fila total
        double totalRev = r.topServices().stream()
                .mapToDouble(s -> s.totalRevenue() != null ? s.totalRevenue().doubleValue() : 0)
                .sum();
        addTotalRow(t, "Total", "", "", fmt(totalRev * 0.82));

        doc.add(t);
    }

    private void buildBarbersTable(Document doc, FullReportResponse r) throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{2.5f, 1.2f, 1.8f, 1.5f, 1.8f});
        t.setWidthPercentage(100);

        addTh(t, "Barbero");
        addTh(t, "Ventas");
        addTh(t, "Total Ventas");
        addTh(t, "Ticket Prom.");
        addTh(t, "Ganancia Neta");

        boolean alt = false;
        for (var b : r.barberPerformance()) {
            double rev = b.revenue() != null ? b.revenue().doubleValue() : 0;
            double avg = b.totalSales() > 0 ? rev / b.totalSales() : 0;
            addTd(t, b.barberName() != null ? b.barberName() : "—", alt, false);
            addTd(t, String.valueOf(b.totalSales()), alt, false);
            addTd(t, fmt(rev), alt, true);
            addTd(t, fmt(avg), alt, false);
            addTdGreen(t, fmt(rev * 0.41), alt);
            alt = !alt;
        }
        doc.add(t);
    }

    private void buildPaymentTable(Document doc, FullReportResponse r) throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{2f, 1.2f, 2f});
        t.setWidthPercentage(55);

        addTh(t, "Método");
        addTh(t, "Ventas");
        addTh(t, "Total");

        boolean alt = false;
        for (var pm : r.paymentMethods()) {
            String method = switch (pm.paymentMethod() != null ? pm.paymentMethod() : "") {
                case "cash"     -> "Efectivo";
                case "card"     -> "Tarjeta";
                case "transfer" -> "Transferencia";
                default         -> pm.paymentMethod() != null ? pm.paymentMethod() : "—";
            };
            addTd(t, method, alt, false);
            addTd(t, String.valueOf(pm.totalSales()), alt, false);
            addTd(t, fmt(pm.revenue() != null ? pm.revenue().doubleValue() : 0), alt, true);
            alt = !alt;
        }
        doc.add(t);
    }

    private void buildAppointmentTable(Document doc, FullReportResponse r) throws DocumentException {
        var am = r.appointmentMetrics();
        PdfPTable t = new PdfPTable(new float[]{2.5f, 1f});
        t.setWidthPercentage(45);

        addTh(t, "Métrica");
        addTh(t, "Valor");

        String[][] rows = {
                { "Total citas",          String.valueOf(am.totalAppointments()) },
                { "Citas completadas",    String.valueOf(am.completed()) },
                { "Citas canceladas",     String.valueOf(am.cancelled()) },
                { "No asistió",           String.valueOf(am.noShow()) },
                { "Tasa no-show",         String.format("%.1f%%", am.noShowRate()) },
                { "Tasa ocupación",       am.totalAppointments() > 0
                        ? String.format("%.1f%%", (double) am.completed() / am.totalAppointments() * 100)
                        : "0%" }
        };
        boolean alt = false;
        for (var row : rows) {
            addTd(t, row[0], alt, false);
            addTd(t, row[1], alt, false);
            alt = !alt;
        }
        doc.add(t);
    }

    private void buildFooter(Document doc, String shopName) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        addRule(doc, TEXT_MUTED);
        Paragraph p = new Paragraph(
                shopName + "  ·  Generado el " +
                        LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                        "  ·  Sistema de gestión de barbería",
                F_SMALL);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingBefore(5);
        doc.add(p);
    }

    // ─────────────────────────────────────────────────────────
    //  HELPERS DE CONSTRUCCIÓN
    // ─────────────────────────────────────────────────────────

    private void addSectionTitle(Document doc, String text) throws DocumentException {
        Paragraph p = new Paragraph(text, F_SECTION);
        p.setSpacingBefore(4);
        p.setSpacingAfter(5);
        doc.add(p);
    }

    private void addRule(Document doc, BaseColor color) throws DocumentException {
        doc.add(new Chunk(new LineSeparator(0.8f, 100f, color, Element.ALIGN_CENTER, 0)));
        doc.add(Chunk.NEWLINE);
    }

    private void addMetricCard(PdfPTable t, String value, String label) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(SURFACE);
        cell.setBorderColor(BORDER_TH);
        cell.setBorderWidth(0.5f);
        cell.setPadding(10);
        Paragraph v = new Paragraph(value, F_MET_V);
        v.setAlignment(Element.ALIGN_LEFT);
        cell.addElement(v);
        Paragraph l = new Paragraph(label, F_MET_L);
        l.setAlignment(Element.ALIGN_LEFT);
        cell.addElement(l);
        t.addCell(cell);
    }

    private void addTh(PdfPTable t, String text) {
        PdfPCell c = new PdfPCell(new Phrase(text.toUpperCase(), F_TH));
        c.setBackgroundColor(SURFACE2);
        c.setBorderColor(BORDER_TH);
        c.setBorderWidth(0.5f);
        c.setPadding(7);
        t.addCell(c);
    }

    private void addTd(PdfPTable t, String text, boolean alt, boolean gold) {
        PdfPCell c = new PdfPCell(new Phrase(text, gold ? F_TD_GOLD : F_TD));
        c.setBackgroundColor(alt ? new BaseColor(20,20,28) : SURFACE);
        c.setBorderColor(BORDER);
        c.setBorderWidth(0.3f);
        c.setPadding(6);
        t.addCell(c);
    }

    private void addTdGreen(PdfPTable t, String text, boolean alt) {
        PdfPCell c = new PdfPCell(new Phrase(text, F_TD_GRN));
        c.setBackgroundColor(alt ? new BaseColor(20,20,28) : SURFACE);
        c.setBorderColor(BORDER);
        c.setBorderWidth(0.3f);
        c.setPadding(6);
        t.addCell(c);
    }

    private void addTotalRow(PdfPTable t, String... cells) {
        Font boldGold = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, GOLD);
        Font boldText = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, TEXT_COLOR);
        for (int i = 0; i < cells.length; i++) {
            boolean isLast = i == cells.length - 1;
            PdfPCell c = new PdfPCell(new Phrase(cells[i], isLast ? boldGold : boldText));
            c.setBackgroundColor(new BaseColor(25, 25, 35));
            c.setBorderColor(BORDER_TH);
            c.setBorderWidth(0.5f);
            c.setPadding(6);
            t.addCell(c);
        }
    }

    private String fmt(double v) {
        return String.format("$%,.2f", v);
    }

    // ─────────────────────────────────────────────────────────
    //  Fondo oscuro en cada página
    // ─────────────────────────────────────────────────────────

    private static class DarkBackground extends PdfPageEventHelper {
        @Override
        public void onStartPage(PdfWriter w, Document d) {
            PdfContentByte cb = w.getDirectContentUnder();
            cb.saveState();
            cb.setColorFill(new BaseColor(13, 13, 20));
            cb.rectangle(0, 0, d.getPageSize().getWidth(), d.getPageSize().getHeight());
            cb.fill();
            cb.restoreState();
        }
    }
}