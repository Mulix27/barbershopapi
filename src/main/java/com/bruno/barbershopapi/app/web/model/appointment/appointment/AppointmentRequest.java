package com.bruno.barbershopapi.app.web.model.appointment.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Schema(description = "Datos para agendar una cita")
public record AppointmentRequest(

        @Schema(description = "Nombre del cliente", example = "Juan Pérez")
        @NotBlank @Size(max = 120)
        String clientName,

        @Schema(description = "Teléfono del cliente", example = "6441234567")
        @NotBlank
        String clientPhone,

        String clientNotes,
        UUID clientId,
        UUID haircutCatalogId,
        String serviceNotes,

        UUID serviceCategoryId,
        UUID serviceVariantId,
        String serviceName,
        BigDecimal servicePrice,
        Integer serviceDurationMin,

        @NotNull
        LocalDate appointmentDate,

        @NotNull
        LocalTime startTime,

        @Schema(allowableValues = {"web", "whatsapp", "dashboard"})
        String source,

        @Schema(description = "ID del barbero asignado. Si es null el sistema asigna uno disponible automáticamente.")
        UUID assignedToId   // ← NUEVO — opcional
) {}