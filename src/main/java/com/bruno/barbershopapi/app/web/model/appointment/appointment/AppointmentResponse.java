package com.bruno.barbershopapi.app.web.model.appointment.appointment;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Cita agendada")
public record AppointmentResponse(
        UUID id,
        String clientName,
        String clientPhone,
        String clientNotes,
        UUID clientId,
        String assignedToName,
        UUID assignedToId,
        String haircutName,
        String serviceNotes,

        UUID serviceCategoryId,
        UUID serviceVariantId,
        String serviceName,
        BigDecimal servicePrice,
        Integer serviceDurationMin,
        UUID saleId,

        LocalDate appointmentDate,
        LocalTime startTime,
        LocalTime endTime,
        String status,
        String source,
        Boolean reminderSent,
        OffsetDateTime createdAt
) {}