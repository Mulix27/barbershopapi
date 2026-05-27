package com.bruno.barbershopapi.app.web.model.report.client;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Métricas de clientes en el período")
public record ClientMetricsResponse(
        @Schema(description = "Clientes que visitaron por primera vez") Long newClients,
        @Schema(description = "Clientes con más de 1 visita en el período") Long recurringClients
) {}