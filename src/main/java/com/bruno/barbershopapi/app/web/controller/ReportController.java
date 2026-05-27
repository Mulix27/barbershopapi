package com.bruno.barbershopapi.app.web.controller;

import com.bruno.barbershopapi.app.facade.ReportFacade;
import com.bruno.barbershopapi.app.web.model.ApiResponse;
import com.bruno.barbershopapi.app.web.model.report.*;
import com.bruno.barbershopapi.app.web.model.report.appointment.AppointmentMetricsResponse;
import com.bruno.barbershopapi.app.web.model.report.appointment.PeakHourResponse;
import com.bruno.barbershopapi.app.web.model.report.barber.BarberPerformanceResponse;
import com.bruno.barbershopapi.app.web.model.report.sales.SalesSummaryResponse;
import com.bruno.barbershopapi.app.web.model.report.service.TopServiceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reportes", description = "Estadísticas y métricas del negocio")
public class ReportController {

    private final ReportFacade reportFacade;

    // ── Reporte completo (todo en uno) ─────────────────────────

    @Operation(
            summary = "Reporte completo",
            description = "Retorna todas las métricas en una sola llamada. " +
                    "Ideal para cargar el dashboard principal de reportes en Angular."
    )
    @GetMapping("/full")
    public ResponseEntity<ApiResponse<FullReportResponse>> getFullReport(
            @Parameter(description = "Fecha inicio", example = "2025-05-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Fecha fin", example = "2025-05-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        ApiResponse<FullReportResponse> res = reportFacade.getFullReport(from, to);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    // ── Resumen de ventas ──────────────────────────────────────

    @Operation(
            summary = "Resumen de ventas",
            description = "Total facturado, número de ventas y ticket promedio en el período."
    )
    @GetMapping("/sales/summary")
    public ResponseEntity<ApiResponse<SalesSummaryResponse>> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        ApiResponse<SalesSummaryResponse> res = reportFacade.getSummary(from, to);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    // ── Servicios más vendidos ─────────────────────────────────

    @Operation(
            summary = "Servicios más vendidos",
            description = "Top N servicios ordenados por cantidad vendida. Default: top 10."
    )
    @GetMapping("/services/top")
    public ResponseEntity<ApiResponse<List<TopServiceResponse>>> getTopServices(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Número de servicios a retornar", example = "10")
            @RequestParam(defaultValue = "10") int limit) {

        ApiResponse<List<TopServiceResponse>> res = reportFacade.getTopServices(from, to, limit);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    // ── Rendimiento por barbero ────────────────────────────────

    @Operation(
            summary = "Rendimiento por barbero",
            description = "Ventas, ingresos y servicios realizados por cada barbero."
    )
    @GetMapping("/barbers/performance")
    public ResponseEntity<ApiResponse<List<BarberPerformanceResponse>>> getBarberPerformance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        ApiResponse<List<BarberPerformanceResponse>> res = reportFacade.getBarberPerformance(from, to);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    // ── Métricas de citas ──────────────────────────────────────

    @Operation(
            summary = "Métricas de citas",
            description = "Total de citas, completadas, canceladas, no-show y tasa de ausentismo."
    )
    @GetMapping("/appointments/metrics")
    public ResponseEntity<ApiResponse<AppointmentMetricsResponse>> getAppointmentMetrics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        ApiResponse<AppointmentMetricsResponse> res = reportFacade.getAppointmentMetrics(from, to);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    // ── Horas pico ─────────────────────────────────────────────

    @Operation(
            summary = "Horas pico",
            description = "Franjas horarias con mayor número de citas. Útil para planear horarios."
    )
    @GetMapping("/appointments/peak-hours")
    public ResponseEntity<ApiResponse<List<PeakHourResponse>>> getPeakHours(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        ApiResponse<List<PeakHourResponse>> res = reportFacade.getPeakHours(from, to);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    // ── Atajos de período ──────────────────────────────────────

    @Operation(summary = "Reporte del día de hoy")
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<FullReportResponse>> today() {
        LocalDate today = LocalDate.now();
        ApiResponse<FullReportResponse> res = reportFacade.getFullReport(today, today);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    @Operation(summary = "Reporte de la semana actual")
    @GetMapping("/this-week")
    public ResponseEntity<ApiResponse<FullReportResponse>> thisWeek() {
        LocalDate today  = LocalDate.now();
        LocalDate monday = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate sunday = today.with(java.time.DayOfWeek.SUNDAY);
        ApiResponse<FullReportResponse> res = reportFacade.getFullReport(monday, sunday);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    @Operation(summary = "Reporte del mes actual")
    @GetMapping("/this-month")
    public ResponseEntity<ApiResponse<FullReportResponse>> thisMonth() {
        LocalDate today     = LocalDate.now();
        LocalDate firstDay  = today.withDayOfMonth(1);
        LocalDate lastDay   = today.withDayOfMonth(today.lengthOfMonth());
        ApiResponse<FullReportResponse> res = reportFacade.getFullReport(firstDay, lastDay);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }
}