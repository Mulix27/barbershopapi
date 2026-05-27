package com.bruno.barbershopapi.app.facade;

import com.bruno.barbershopapi.app.service.ReportService;
import com.bruno.barbershopapi.app.web.model.ApiResponse;
import com.bruno.barbershopapi.app.web.model.report.*;
import com.bruno.barbershopapi.app.web.model.report.appointment.AppointmentMetricsResponse;
import com.bruno.barbershopapi.app.web.model.report.appointment.PeakHourResponse;
import com.bruno.barbershopapi.app.web.model.report.barber.BarberPerformanceResponse;
import com.bruno.barbershopapi.app.web.model.report.sales.SalesSummaryResponse;
import com.bruno.barbershopapi.app.web.model.report.service.TopServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.bruno.barbershopapi.app.domain.repository.BarbershopRepository;
import com.bruno.barbershopapi.app.service.ReportPdfService;
import com.bruno.barbershopapi.util.TenantContext;

import java.time.format.DateTimeFormatter;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportFacade {

    private final ReportService reportService;
    private final ReportPdfService reportPdfService;
    private final BarbershopRepository barbershopRepository;

    public ApiResponse<FullReportResponse> getFullReport(LocalDate from, LocalDate to) {
        try {
            validateRange(from, to);
            return ApiResponse.ok(reportService.getFullReport(from, to));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<SalesSummaryResponse> getSummary(LocalDate from, LocalDate to) {
        try {
            validateRange(from, to);
            return ApiResponse.ok(reportService.getSummary(from, to));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<List<TopServiceResponse>> getTopServices(LocalDate from, LocalDate to, int limit) {
        try {
            validateRange(from, to);
            return ApiResponse.ok(reportService.getTopServices(from, to, limit));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<List<BarberPerformanceResponse>> getBarberPerformance(LocalDate from, LocalDate to) {
        try {
            validateRange(from, to);
            return ApiResponse.ok(reportService.getBarberPerformance(from, to));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<AppointmentMetricsResponse> getAppointmentMetrics(LocalDate from, LocalDate to) {
        try {
            validateRange(from, to);
            return ApiResponse.ok(reportService.getAppointmentMetrics(from, to));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<List<PeakHourResponse>> getPeakHours(LocalDate from, LocalDate to) {
        try {
            validateRange(from, to);
            return ApiResponse.ok(reportService.getPeakHours(from, to));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<PdfReportResponse> generatePdf(PdfReportRequest req) {
        try {
            LocalDate to = LocalDate.now();

            LocalDate from = switch (req.period()) {
                case "today" -> to;
                case "week" -> to.with(java.time.DayOfWeek.MONDAY);
                case "month" -> to.withDayOfMonth(1);
                case "custom" -> LocalDate.parse(req.from());
                default -> to.withDayOfMonth(1);
            };

            if ("custom".equals(req.period()) && req.to() != null) {
                to = LocalDate.parse(req.to());
            }

            validateRange(from, to);

            String periodLabel = switch (req.period()) {
                case "today" -> "Hoy";
                case "week" -> "Esta semana";
                case "month" -> "Este mes";
                case "custom" -> from + " — " + to;
                default -> "Este mes";
            };

            FullReportResponse report = reportService.getFullReport(from, to);

            String shopName = barbershopRepository
                    .findById(TenantContext.get())
                    .map(b -> b.getName())
                    .orElse("Barbería");

            String base64 = reportPdfService.generateBase64(report, shopName);

            String fileName = "Reporte_" +
                    periodLabel.replace(" ", "_").replace("—", "-") + "_" +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                    ".pdf";

            return ApiResponse.ok(new PdfReportResponse(base64, fileName, periodLabel));

        } catch (Exception e) {
            log.error("Error generando PDF: {}", e.getMessage(), e);
            return ApiResponse.error("Error al generar el reporte: " + e.getMessage());
        }
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new RuntimeException("La fecha de inicio no puede ser mayor a la fecha fin");
        }
        if (from.isBefore(LocalDate.now().minusYears(2))) {
            throw new RuntimeException("El rango máximo de consulta es 2 años");
        }
    }
}