package com.bruno.barbershopapi.app.web.model.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Datos para crear o actualizar un producto")
public record ProductRequest(

        @NotBlank(message = "El nombre es requerido")
        @Size(max = 120)
        String name,

        String sku,
        String description,

        @NotNull(message = "El precio es requerido")
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal price,

        BigDecimal cost,

        @Schema(description = "Stock inicial", example = "10")
        Integer stock,

        @Schema(description = "Stock mínimo antes de alertar", example = "5")
        Integer stockMin,

        @Schema(description = "URL de la imagen del producto")
        String imageUrl
) {}