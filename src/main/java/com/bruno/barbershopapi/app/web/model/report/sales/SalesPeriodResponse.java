package com.bruno.barbershopapi.app.web.model.report.sales;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Ventas agrupadas por un período")
public record SalesPeriodResponse(
        @Schema(description = "Etiqueta del período", example = "2025-05-01")
        String period,
        Long totalSales,
        BigDecimal revenue
) {}