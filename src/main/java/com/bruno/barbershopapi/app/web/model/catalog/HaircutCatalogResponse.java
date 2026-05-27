package com.bruno.barbershopapi.app.web.model.catalog;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Servicio del catálogo")
public record HaircutCatalogResponse(
        UUID id,
        String name,
        String type,
        String description,
        BigDecimal price,
        Integer durationMin,
        Boolean isActive
) {}