package com.bruno.barbershopapi.app.web.model.catalog;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Variante o estilo específico de un servicio")
public record ServiceVariantResponse(

        UUID id,
        String name,
        String description,
        BigDecimal price,
        Integer durationMin,
        Integer sortOrder,
        boolean isActive,
        OffsetDateTime createdAt

) {}