package com.bruno.barbershopapi.app.web.model.report.sales;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Ventas por día de la semana")
public record DayOfWeekResponse(
        String dayName,
        Long totalSales,
        BigDecimal revenue
) {}