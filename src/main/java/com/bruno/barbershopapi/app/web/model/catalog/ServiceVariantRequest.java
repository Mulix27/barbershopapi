package com.bruno.barbershopapi.app.web.model.catalog;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Request para crear o actualizar una variante de servicio")
public record ServiceVariantRequest(

        UUID id,

        @NotBlank(message = "El nombre de la variante es requerido")
        @Size(max = 100)
        String name,

        String description,

        @NotNull
        @DecimalMin(
                value = "0.01",
                message = "El precio debe ser mayor a 0"
        )
        BigDecimal price,

        @NotNull
        @Min(value = 1)
        Integer durationMin,

        Integer sortOrder,

        Boolean isActive

) {}