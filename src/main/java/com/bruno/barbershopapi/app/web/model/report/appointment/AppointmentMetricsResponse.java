package com.bruno.barbershopapi.app.web.model.report.appointment;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Métricas de citas en el período")
public record AppointmentMetricsResponse(
        Long totalAppointments,
        Long completed,
        Long cancelled,
        Long noShow,
        @Schema(description = "Porcentaje de no-show sobre el total")
        Double noShowRate
) {}