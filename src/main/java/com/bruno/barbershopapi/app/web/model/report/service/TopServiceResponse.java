package com.bruno.barbershopapi.app.web.model.report.service;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Servicio más vendido")
public record TopServiceResponse(
        String serviceName,
        Long totalQuantity,
        BigDecimal totalRevenue
) {}