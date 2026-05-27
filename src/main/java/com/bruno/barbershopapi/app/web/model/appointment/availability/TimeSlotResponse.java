package com.bruno.barbershopapi.app.web.model.appointment.availability;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;

@Schema(description = "Slot de tiempo disponible para agendar")
public record TimeSlotResponse(
        LocalTime startTime,
        LocalTime endTime,
        boolean available
) {}