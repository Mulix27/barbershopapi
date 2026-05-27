package com.bruno.barbershopapi.app.web.controller;

import com.bruno.barbershopapi.app.facade.ReportFacade;
import com.bruno.barbershopapi.app.web.model.ApiResponse;
import com.bruno.barbershopapi.app.web.model.report.PdfReportRequest;
import com.bruno.barbershopapi.app.web.model.report.PdfReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reportes", description = "Estadísticas y métricas del negocio")
public class ReportPdfController {

    private final ReportFacade reportFacade;

    /**
     * POST /api/reports/pdf
     * Genera el reporte PDF del período indicado y retorna el contenido en Base64.
     */
    @Operation(
            summary     = "Generar PDF del reporte",
            description = "Calcula el reporte del período, genera un PDF dark-theme y lo retorna en Base64. " +
                    "El frontend convierte el Base64 a Blob y lanza la descarga automáticamente."
    )
    @PostMapping("/pdf")
    public ResponseEntity<ApiResponse<PdfReportResponse>> generatePdf(
            @Valid @RequestBody PdfReportRequest req) {

        ApiResponse<PdfReportResponse> res = reportFacade.generatePdf(req);
        return ResponseEntity.status(res.success() ? 200 : 500).body(res);
    }
}