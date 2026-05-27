package com.bruno.barbershopapi.app.web.model.appointment.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalTime;

@Schema(description = "Horario de trabajo de un barbero")
public record BarberScheduleRequest(

        @NotNull @Min(1) @Max(7)
        Short dayOfWeek,

        @NotNull
        LocalTime startTime,

        @NotNull
        LocalTime endTime,

        Short slotDuration,

        Boolean isActive
) {}