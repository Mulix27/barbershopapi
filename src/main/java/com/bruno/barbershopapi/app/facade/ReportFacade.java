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
            LocalDate today = LocalDate.now();
            LocalDate from;
            LocalDate to;

            switch (req.period()) {
                case "today" -> {
                    from = today;
                    to = today;
                }

                case "week" -> {
                    from = today.with(java.time.DayOfWeek.MONDAY);
                    to = today.with(java.time.DayOfWeek.SUNDAY);
                }

                case "month" -> {
                    from = today.withDayOfMonth(1);
                    to = today.withDayOfMonth(today.lengthOfMonth());
                }

                case "custom" -> {
                    from = LocalDate.parse(req.from());
                    to = req.to() != null ? LocalDate.parse(req.to()) : from;
                }

                default -> {
                    from = today.withDayOfMonth(1);
                    to = today.withDayOfMonth(today.lengthOfMonth());
                }
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

            var barbershop = barbershopRepository
                    .findById(TenantContext.get())
                    .orElseThrow(() -> new RuntimeException("Barbería no encontrada"));

            String shopName = barbershop.getName();

            boolean singleBarber = Boolean.TRUE.equals(barbershop.getSingleBarber());

            String base64 = reportPdfService.generateBase64(report, shopName, singleBarber);

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