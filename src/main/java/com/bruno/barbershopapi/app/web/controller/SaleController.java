package com.bruno.barbershopapi.app.web.controller;

import com.bruno.barbershopapi.app.facade.SaleFacade;
import com.bruno.barbershopapi.app.web.model.ApiResponse;
import com.bruno.barbershopapi.app.web.model.sale.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Ventas", description = "Punto de venta — registra cortes y productos vendidos")
public class SaleController {

    private final SaleFacade saleFacade;

    @Operation(summary = "Listar todas las ventas")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SaleResponse>>> findAll() {
        ApiResponse<List<SaleResponse>> res = saleFacade.findAll();
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    @Operation(summary = "Obtener detalle de una venta")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SaleResponse>> findById(@PathVariable UUID id) {
        ApiResponse<SaleResponse> res = saleFacade.findById(id);
        return ResponseEntity.status(res.success() ? 200 : 404).body(res);
    }

    @Operation(
            summary = "Registrar nueva venta",
            description = "Punto de venta principal. Descuenta stock automáticamente de los productos vendidos."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<SaleResponse>> create(
            @Valid @RequestBody SaleRequest req) {
        ApiResponse<SaleResponse> res = saleFacade.create(req);
        return ResponseEntity.status(res.success() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST).body(res);
    }

    @Operation(
            summary = "Ventas por rango de fechas",
            description = "Filtra ventas completadas entre dos fechas. Útil para reportes."
    )
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<SaleResponse>>> findByRange(
            @Parameter(description = "Fecha inicio (ISO 8601)", example = "2025-01-01T00:00:00Z")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @Parameter(description = "Fecha fin (ISO 8601)", example = "2025-01-31T23:59:59Z")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        ApiResponse<List<SaleResponse>> res = saleFacade.findByDateRange(from, to);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    @Operation(summary = "Historial de ventas de un cliente")
    @GetMapping("/client/{clientId}")
    public ResponseEntity<ApiResponse<List<SaleResponse>>> findByClient(
            @PathVariable UUID clientId) {
        ApiResponse<List<SaleResponse>> res = saleFacade.findByClient(clientId);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    @Operation(summary = "Cancelar una venta", description = "Cancela la venta y devuelve el stock de los productos.")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<SaleResponse>> cancel(@PathVariable UUID id) {
        ApiResponse<SaleResponse> res = saleFacade.cancel(id);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }
}