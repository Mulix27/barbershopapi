package com.bruno.barbershopapi.app.web.model.client;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Foto de un corte del cliente")
public record HaircutPhotoResponse(
        UUID id,
        String url,
        OffsetDateTime takenAt,
        String notes
) {
}