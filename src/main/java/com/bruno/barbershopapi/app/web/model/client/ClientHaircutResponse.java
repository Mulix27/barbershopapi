package com.bruno.barbershopapi.app.web.model.client;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Corte guardado en el perfil del cliente")
public record ClientHaircutResponse(
        UUID id,
        String type,
        String name,
        String description,
        Boolean isPreferred,
        List<HaircutPhotoResponse> photos,
        OffsetDateTime createdAt
) {
}