package com.bruno.barbershopapi.app.web.model.report;

import com.bruno.barbershopapi.app.web.model.report.appointment.AppointmentMetricsResponse;
import com.bruno.barbershopapi.app.web.model.report.appointment.PeakHourResponse;
import com.bruno.barbershopapi.app.web.model.report.barber.BarberPerformanceResponse;
import com.bruno.barbershopapi.app.web.model.report.client.ClientMetricsResponse;
import com.bruno.barbershopapi.app.web.model.report.payment.PaymentMethodResponse;
import com.bruno.barbershopapi.app.web.model.report.sales.DayOfWeekResponse;
import com.bruno.barbershopapi.app.web.model.report.sales.SalesPeriodResponse;
import com.bruno.barbershopapi.app.web.model.report.sales.SalesSummaryResponse;
import com.bruno.barbershopapi.app.web.model.report.service.TopServiceResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Reporte completo de la barbería para un período")
public record FullReportResponse(
        String periodLabel,
        String from,
        String to,

        SalesSummaryResponse summary,
        List<SalesPeriodResponse> salesByDay,
        List<SalesPeriodResponse> salesByWeek,
        List<SalesPeriodResponse> salesByMonth,
        List<TopServiceResponse> topServices,
        List<BarberPerformanceResponse> barberPerformance,
        ClientMetricsResponse clientMetrics,
        List<PaymentMethodResponse> paymentMethods,
        List<DayOfWeekResponse> salesByDayOfWeek,
        AppointmentMetricsResponse appointmentMetrics,
        List<PeakHourResponse> peakHours
) {}