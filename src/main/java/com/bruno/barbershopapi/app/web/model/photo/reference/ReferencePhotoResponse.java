package com.bruno.barbershopapi.app.web.model.photo.reference;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Foto de referencia subida por el cliente al agendar")
public record ReferencePhotoResponse(
        UUID appointmentId,
        String url,
        String publicId,
        OffsetDateTime uploadedAt
) {}