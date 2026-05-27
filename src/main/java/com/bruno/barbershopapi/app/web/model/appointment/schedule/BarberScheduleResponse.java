package com.bruno.barbershopapi.app.web.model.appointment.schedule;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;
import java.util.UUID;

@Schema(description = "Horario guardado del barbero")
public record BarberScheduleResponse(
        UUID id,
        Short dayOfWeek,
        String dayName,
        LocalTime startTime,
        LocalTime endTime,
        Short slotDuration,
        Boolean isActive
) {}