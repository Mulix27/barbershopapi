package com.bruno.barbershopapi.app.web.model.sale;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Venta registrada")
public record SaleResponse(
        UUID id,
        UUID clientId,
        String clientName,
        String attendedBy,
        String paymentMethod,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total,
        String status,
        List<SaleItemResponse> items,
        OffsetDateTime createdAt
) {}