package com.bruno.barbershopapi.app.web.model.report.sales;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Resumen general de ventas en un período")
public record SalesSummaryResponse(
        @Schema(description = "Total facturado") BigDecimal totalRevenue,
        @Schema(description = "Número de ventas") Long totalSales,
        @Schema(description = "Ticket promedio por venta") BigDecimal averageTicket
) {}