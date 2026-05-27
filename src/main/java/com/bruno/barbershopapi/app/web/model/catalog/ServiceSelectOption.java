package com.bruno.barbershopapi.app.web.model.catalog;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Servicio seleccionable para venta o cita (aplanado)")
public record ServiceSelectOption(

        UUID categoryId,
        String categoryName,
        String categoryIcon,
        String pricingMode,

        UUID variantId,

        String variantName,

        BigDecimal price,
        Integer durationMin,

        String displayName

) {}