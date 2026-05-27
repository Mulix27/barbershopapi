package com.bruno.barbershopapi.app.web.model.report.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Ventas desglosadas por método de pago")
public record PaymentMethodResponse(
        @Schema(description = "Método", example = "cash")
        String paymentMethod,
        Long totalSales,
        BigDecimal revenue
) {}