package com.bruno.barbershopapi.app.web.model.catalog;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Datos para crear o actualizar un servicio del catálogo")
public record HaircutCatalogRequest(

        @NotBlank(message = "El nombre es requerido")
        @Size(max = 120)
        String name,

        @Schema(description = "Tipo", example = "hair", allowableValues = {"hair","beard","combo","other"})
        @NotBlank
        String type,

        String description,

        @NotNull(message = "El precio es requerido")
        @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
        BigDecimal price,

        @Schema(description = "Duración estimada en minutos", example = "30")
        Integer durationMin
) {}