package com.bruno.barbershopapi.app.web.model.client;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Datos del cliente")
public record ClientResponse(
        UUID id,
        String fullName,
        String phone,
        String email,
        LocalDate birthDate,
        String notes,
        Boolean isActive,
        OffsetDateTime createdAt,

        @Schema(description = "Cortes guardados del cliente")
        List<ClientHaircutResponse> haircuts
) {
}