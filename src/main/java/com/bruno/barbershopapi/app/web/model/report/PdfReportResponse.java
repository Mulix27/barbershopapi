package com.bruno.barbershopapi.app.web.model.report;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de reporte PDF generado")
public record PdfReportResponse(

        @Schema(
                description = "Archivo PDF en Base64"
        )
        String base64,

        @Schema(
                description = "Nombre del archivo PDF",
                example = "sales-report-may-2026.pdf"
        )
        String fileName,

        @Schema(
                description = "Periodo consultado",
                example = "month"
        )
        String period

) {}