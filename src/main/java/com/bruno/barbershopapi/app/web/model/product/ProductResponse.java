package com.bruno.barbershopapi.app.web.model.product;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Producto del inventario")
public record ProductResponse(
        UUID id,
        String name,
        String sku,
        BigDecimal price,
        BigDecimal cost,
        Integer stock,
        Integer stockMin,
        Boolean isActive,
        String imageUrl,

        @Schema(description = "TRUE si el stock está por debajo del mínimo")
        Boolean lowStock
) {}