package com.bruno.barbershopapi.app.service;

import com.bruno.barbershopapi.app.domain.repository.AppointmentReportRepository;
import com.bruno.barbershopapi.app.domain.repository.ReportRepository;
import com.bruno.barbershopapi.app.web.model.report.*;
import com.bruno.barbershopapi.app.web.model.report.appointment.AppointmentMetricsResponse;
import com.bruno.barbershopapi.app.web.model.report.appointment.PeakHourResponse;
import com.bruno.barbershopapi.app.web.model.report.barber.BarberPerformanceResponse;
import com.bruno.barbershopapi.app.web.model.report.client.ClientMetricsResponse;
import com.bruno.barbershopapi.app.web.model.report.payment.PaymentMethodResponse;
import com.bruno.barbershopapi.app.web.model.report.sales.DayOfWeekResponse;
import com.bruno.barbershopapi.app.web.model.report.sales.SalesPeriodResponse;
import com.bruno.barbershopapi.app.web.model.report.sales.SalesSummaryResponse;
import com.bruno.barbershopapi.app.web.model.report.service.TopServiceResponse;
import com.bruno.barbershopapi.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository            reportRepository;
    private final AppointmentReportRepository appointmentReportRepository;

    // ══════════════════════════════════════════════════════════
    //  REPORTE COMPLETO (dashboard principal)
    // ══════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public FullReportResponse getFullReport(LocalDate from, LocalDate to) {
        UUID shopId = TenantContext.get();
        OffsetDateTime fromDt = from.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime toDt   = to.atTime(23, 59, 59).atOffset(ZoneOffset.UTC);

        String periodLabel = from.equals(to)
                ? from.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : from.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " – " +
                to.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        return new FullReportResponse(
                periodLabel,
                from.toString(),
                to.toString(),
                getSummary(shopId, fromDt, toDt),
                getSalesByDay(shopId, fromDt, toDt),
                getSalesByWeek(shopId, fromDt, toDt),
                getSalesByMonth(shopId, fromDt, toDt),
                getTopServices(shopId, fromDt, toDt, 10),
                getBarberPerformance(shopId, fromDt, toDt),
                getClientMetrics(shopId, fromDt, toDt),
                getPaymentMethods(shopId, fromDt, toDt),
                getSalesByDayOfWeek(shopId, fromDt, toDt),
                getAppointmentMetrics(shopId, from, to),
                getPeakHours(shopId, from, to)
        );
    }

    // ══════════════════════════════════════════════════════════
    //  MÉTODOS INDIVIDUALES (para endpoints específicos)
    // ══════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public SalesSummaryResponse getSummary(LocalDate from, LocalDate to) {
        UUID shopId = TenantContext.get();
        OffsetDateTime fromDt = from.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime toDt   = to.atTime(23, 59, 59).atOffset(ZoneOffset.UTC);
        return getSummary(shopId, fromDt, toDt);
    }

    @Transactional(readOnly = true)
    public List<TopServiceResponse> getTopServices(LocalDate from, LocalDate to, int limit) {
        UUID shopId = TenantContext.get();
        OffsetDateTime fromDt = from.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime toDt   = to.atTime(23, 59, 59).atOffset(ZoneOffset.UTC);
        return getTopServices(shopId, fromDt, toDt, limit);
    }

    @Transactional(readOnly = true)
    public List<BarberPerformanceResponse> getBarberPerformance(LocalDate from, LocalDate to) {
        UUID shopId = TenantContext.get();
        OffsetDateTime fromDt = from.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime toDt   = to.atTime(23, 59, 59).atOffset(ZoneOffset.UTC);
        return getBarberPerformance(shopId, fromDt, toDt);
    }

    @Transactional(readOnly = true)
    public AppointmentMetricsResponse getAppointmentMetrics(LocalDate from, LocalDate to) {
        return getAppointmentMetrics(TenantContext.get(), from, to);
    }

    @Transactional(readOnly = true)
    public List<PeakHourResponse> getPeakHours(LocalDate from, LocalDate to) {
        return getPeakHours(TenantContext.get(), from, to);
    }

    // ══════════════════════════════════════════════════════════
    //  HELPERS PRIVADOS — mapean Object[] a records
    // ══════════════════════════════════════════════════════════

    private SalesSummaryResponse getSummary(UUID shopId, OffsetDateTime from, OffsetDateTime to) {
        List<Object[]> rows = reportRepository.getSalesSummary(shopId, from, to);

        if (rows == null || rows.isEmpty()) {
            return new SalesSummaryResponse(BigDecimal.ZERO, 0L, BigDecimal.ZERO);
        }

        Object[] row = unwrapRow(rows.get(0));

        return new SalesSummaryResponse(
                toBigDecimal(row[0]),
                toLong(row[1]),
                toBigDecimal(row[2])
        );
    }

    private List<SalesPeriodResponse> getSalesByDay(UUID shopId, OffsetDateTime from, OffsetDateTime to) {
        return reportRepository.getSalesByDay(shopId, from, to)
                .stream()
                .map(r -> {
                    Object[] row = unwrapRow(r);
                    return new SalesPeriodResponse(
                            row[0].toString(),
                            toLong(row[1]),
                            toBigDecimal(row[2])
                    );
                })
                .toList();
    }

    private List<SalesPeriodResponse> getSalesByWeek(UUID shopId, OffsetDateTime from, OffsetDateTime to) {
        return reportRepository.getSalesByWeek(shopId, from, to)
                .stream()
                .map(r -> {
                    Object[] row = unwrapRow(r);
                    return new SalesPeriodResponse(
                            row[0].toString(),
                            toLong(row[1]),
                            toBigDecimal(row[2])
                    );
                })
                .toList();
    }

    private List<SalesPeriodResponse> getSalesByMonth(UUID shopId, OffsetDateTime from, OffsetDateTime to) {
        return reportRepository.getSalesByMonth(shopId, from, to)
                .stream()
                .map(r -> {
                    Object[] row = unwrapRow(r);
                    return new SalesPeriodResponse(
                            row[0].toString(),
                            toLong(row[1]),
                            toBigDecimal(row[2])
                    );
                })
                .toList();
    }

    private List<TopServiceResponse> getTopServices(UUID shopId, OffsetDateTime from, OffsetDateTime to, int limit) {
        return reportRepository.getTopServices(shopId, from, to, limit)
                .stream()
                .map(r -> {
                    Object[] row = unwrapRow(r);
                    return new TopServiceResponse(
                            row[0].toString(),
                            toLong(row[1]),
                            toBigDecimal(row[2])
                    );
                })
                .toList();
    }

    private List<BarberPerformanceResponse> getBarberPerformance(UUID shopId, OffsetDateTime from, OffsetDateTime to) {
        return reportRepository.getSalesByBarber(shopId, from, to)
                .stream()
                .map(r -> {
                    Object[] row = unwrapRow(r);
                    return new BarberPerformanceResponse(
                            row[0].toString(),
                            toLong(row[1]),
                            toBigDecimal(row[2]),
                            toLong(row[3])
                    );
                })
                .toList();
    }

    private ClientMetricsResponse getClientMetrics(UUID shopId, OffsetDateTime from, OffsetDateTime to) {
        Long newClients       = reportRepository.getNewClientsCount(shopId, from, to);
        Long recurringClients = reportRepository.getRecurringClientsCount(shopId, from, to);
        return new ClientMetricsResponse(
                newClients != null ? newClients : 0L,
                recurringClients != null ? recurringClients : 0L
        );
    }

    private List<PaymentMethodResponse> getPaymentMethods(UUID shopId, OffsetDateTime from, OffsetDateTime to) {
        return reportRepository.getSalesByPaymentMethod(shopId, from, to)
                .stream()
                .map(r -> {
                    Object[] row = unwrapRow(r);
                    return new PaymentMethodResponse(
                            row[0].toString(),
                            toLong(row[1]),
                            toBigDecimal(row[2])
                    );
                })
                .toList();
    }

    private List<DayOfWeekResponse> getSalesByDayOfWeek(UUID shopId, OffsetDateTime from, OffsetDateTime to) {
        return reportRepository.getSalesByDayOfWeek(shopId, from, to)
                .stream()
                .map(r -> {
                    Object[] row = unwrapRow(r);
                    return new DayOfWeekResponse(
                            row[0].toString().trim(),
                            toLong(row[2]),
                            toBigDecimal(row[3])
                    );
                })
                .toList();
    }

    private AppointmentMetricsResponse getAppointmentMetrics(UUID shopId, LocalDate from, LocalDate to) {
        Object[] row = unwrapRow(appointmentReportRepository.getAppointmentMetrics(shopId, from, to));

        long total     = toLong(row[0]);
        long completed = toLong(row[1]);
        long cancelled = toLong(row[2]);
        long noShow    = toLong(row[3]);

        double noShowRate = total > 0
                ? BigDecimal.valueOf((double) noShow / total * 100)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue()
                : 0.0;

        return new AppointmentMetricsResponse(total, completed, cancelled, noShow, noShowRate);
    }

    private List<PeakHourResponse> getPeakHours(UUID shopId, LocalDate from, LocalDate to) {
        return appointmentReportRepository.getPeakHours(shopId, from, to)
                .stream()
                .map(r -> {
                    Object[] row = unwrapRow(r);
                    int hour = ((Number) row[0]).intValue();

                    String label = String.format("%02d:00 - %02d:00", hour, hour + 1);

                    return new PeakHourResponse(hour, label, toLong(row[1]));
                })
                .toList();
    }

    // ── Conversores seguros ────────────────────────────────────

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(value.toString().trim());
    }

    private Long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number n) return n.longValue();
        return Long.parseLong(value.toString().trim());
    }

    private Object[] unwrapRow(Object result) {
        if (result instanceof Object[] arr) {
            while (arr.length == 1 && arr[0] instanceof Object[]) {
                arr = (Object[]) arr[0];
            }
            return arr;
        }
        return new Object[]{ result };
    }
}