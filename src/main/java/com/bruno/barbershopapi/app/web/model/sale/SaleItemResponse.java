package com.bruno.barbershopapi.app.web.model.sale;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Item de venta registrado")
public record SaleItemResponse(
        UUID id,
        String itemType,
        String itemName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal total
) {}