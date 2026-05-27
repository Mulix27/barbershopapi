package com.bruno.barbershopapi.app.web.model.photo.haircut;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Foto de corte subida")
public record HaircutPhotoResponse(
        UUID id,
        String url,              // URL pública para mostrar en Angular
        String publicId,         // ID en Cloudinary (para borrar)
        String storageProvider,
        OffsetDateTime takenAt,
        String notes,
        String uploadedBy        // nombre del barbero que subió la foto
) {}