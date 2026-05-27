package com.bruno.barbershopapi.app.web.model.report.appointment;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Franja horaria con más citas")
public record PeakHourResponse(
        @Schema(description = "Hora del día (0-23)", example = "10")
        Integer hour,
        @Schema(description = "Etiqueta legible", example = "10:00 - 11:00")
        String label,
        Long totalAppointments
) {}