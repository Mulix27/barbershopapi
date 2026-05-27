package com.bruno.barbershopapi.app.web.model.appointment.blocked;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Bloquear tiempo en agenda")
public record BlockedTimeRequest(

        @NotNull
        LocalDate blockedDate,

        LocalTime startTime,
        LocalTime endTime,
        String reason
) {}