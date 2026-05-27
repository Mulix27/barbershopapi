package com.bruno.barbershopapi.app.web.model.appointment.availability;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Disponibilidad de un día completo")
public record DayAvailabilityResponse(
        LocalDate date,
        String dayName,
        boolean barbershopOpen,
        List<TimeSlotResponse> slots
) {}