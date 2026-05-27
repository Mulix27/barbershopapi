package com.bruno.barbershopapi.app.web.model.sale;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(description = "Datos para registrar una venta")
public record SaleRequest(

        @Schema(description = "ID del cliente registrado (opcional para clientes de paso)")
        UUID clientId,

        @Schema(description = "ID del barbero que atendió (opcional)")
        UUID attendedByUserId,

        @Schema(description = "Método de pago", example = "cash",
                allowableValues = {"cash","card","transfer","other"})
        @NotBlank(message = "El método de pago es requerido")
        String paymentMethod,

        @Schema(description = "Descuento aplicado", example = "0.00")
        BigDecimal discount,

        String notes,

        @NotEmpty(message = "La venta debe tener al menos un item")
        List<SaleItemRequest> items
) {}