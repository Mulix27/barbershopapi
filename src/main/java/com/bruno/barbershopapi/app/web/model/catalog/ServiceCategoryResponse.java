package com.bruno.barbershopapi.app.web.model.catalog;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Categoría de servicio con sus variantes")
public record ServiceCategoryResponse(

        UUID id,
        String name,
        String icon,
        String pricingMode,
        BigDecimal basePrice,
        Integer baseDuration,
        Integer sortOrder,
        boolean isActive,
        OffsetDateTime createdAt,
        List<ServiceVariantResponse> variants

) {}