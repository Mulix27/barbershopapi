package com.bruno.barbershopapi.app.web.model.sale;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.UUID;

@Schema(description = "Item dentro de una venta")
public record SaleItemRequest(

        @Schema(description = "Tipo de item", allowableValues = {"service","product"})
        @NotBlank(message = "El tipo de item es requerido")
        String itemType,

        @Schema(description = "ID del servicio (haircut_catalog) o producto")
        @NotNull(message = "El ID del item es requerido")
        UUID itemRefId,

        @Schema(description = "Cantidad", example = "1")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        Integer quantity
) {}