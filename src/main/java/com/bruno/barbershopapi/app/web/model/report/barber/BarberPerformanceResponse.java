package com.bruno.barbershopapi.app.web.model.report.barber;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Rendimiento de un barbero en el período")
public record BarberPerformanceResponse(
        String barberName,
        Long totalSales,
        BigDecimal revenue,
        Long totalServices
) {}