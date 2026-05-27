package com.bruno.barbershopapi.app.web.controller;

import com.bruno.barbershopapi.app.facade.CatalogFacade;
import com.bruno.barbershopapi.app.web.model.ApiResponse;
import com.bruno.barbershopapi.app.web.model.catalog.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Catálogo", description = "Servicios que ofrece la barbería (cortes, barbas, combos)")
public class CatalogController {

    private final CatalogFacade catalogFacade;

    @Operation(summary = "Listar servicios activos del catálogo")
    @GetMapping
    public ResponseEntity<ApiResponse<List<HaircutCatalogResponse>>> findAll() {
        ApiResponse<List<HaircutCatalogResponse>> res = catalogFacade.findAll();
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    @Operation(summary = "Obtener servicio por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HaircutCatalogResponse>> findById(@PathVariable UUID id) {
        ApiResponse<HaircutCatalogResponse> res = catalogFacade.findById(id);
        return ResponseEntity.status(res.success() ? 200 : 404).body(res);
    }

    @Operation(summary = "Crear servicio en el catálogo")
    @PostMapping
    public ResponseEntity<ApiResponse<HaircutCatalogResponse>> create(
            @Valid @RequestBody HaircutCatalogRequest req) {
        ApiResponse<HaircutCatalogResponse> res = catalogFacade.create(req);
        return ResponseEntity.status(res.success() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST).body(res);
    }

    @Operation(summary = "Actualizar servicio del catálogo")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HaircutCatalogResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody HaircutCatalogRequest req) {
        ApiResponse<HaircutCatalogResponse> res = catalogFacade.update(id, req);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    @Operation(summary = "Activar o desactivar un servicio del catálogo")
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggle(@PathVariable UUID id) {
        ApiResponse<Void> res = catalogFacade.toggleActive(id);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }
}