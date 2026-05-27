package com.bruno.barbershopapi.app.web.model.catalog;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Request para crear o actualizar una categoría de servicio")
public record ServiceCategoryRequest(
        @NotBlank(message = "El nombre es requerido")
        @Size(max = 100)
        String name,

        @Size(max = 50)
        String icon,

        @NotBlank
        @Pattern(regexp = "fixed|variants", message = "pricingMode debe ser 'fixed' o 'variants'")
        String pricingMode,

        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
        BigDecimal basePrice,

        @Min(value = 1, message = "La duración debe ser mayor a 0")
        Integer baseDuration,

        Integer sortOrder,

        @Valid
        List<ServiceVariantRequest> variants
) {}