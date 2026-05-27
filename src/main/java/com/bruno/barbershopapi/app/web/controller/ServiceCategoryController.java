package com.bruno.barbershopapi.app.web.controller;

import com.bruno.barbershopapi.app.service.ServiceCategoryService;
import com.bruno.barbershopapi.app.web.model.ApiResponse;
import com.bruno.barbershopapi.app.web.model.catalog.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/catalog/categories")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Catálogo v2", description = "Gestión de servicios con categorías y variantes")
public class ServiceCategoryController {

    private final ServiceCategoryService service;

    // ── GET /api/catalog/categories ───────────────────────────
    @Operation(
            summary = "Listar todas las categorías de servicio",
            description = "Retorna las categorías con sus variantes. " +
                    "Usa ?onlyActive=false para incluir las desactivadas."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceCategoryResponse>>> getAll(
            @RequestParam(defaultValue = "true") boolean onlyActive) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(service.getAll(onlyActive)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── GET /api/catalog/categories/options ───────────────────
    @Operation(
            summary = "Lista aplanada para selectores",
            description = "Retorna cada opción seleccionable en ventas y citas. " +
                    "Para 'fixed': una opción por categoría. " +
                    "Para 'variants': una opción por variante activa."
    )
    @GetMapping("/options")
    public ResponseEntity<ApiResponse<List<ServiceSelectOption>>> getOptions() {
        try {
            return ResponseEntity.ok(ApiResponse.ok(service.getSelectOptions()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── POST /api/catalog/categories ─────────────────────────
    @Operation(
            summary = "Crear nueva categoría de servicio",
            description = "Crea una categoría con modo 'fixed' (precio único) " +
                    "o 'variants' (precio por estilo/variante)."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<ServiceCategoryResponse>> create(
            @Valid @RequestBody ServiceCategoryRequest req) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(service.create(req)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── PUT /api/catalog/categories/{id} ─────────────────────
    @Operation(summary = "Actualizar categoría de servicio")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceCategoryResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ServiceCategoryRequest req) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(service.update(id, req)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── PATCH /api/catalog/categories/{id}/toggle ─────────────
    @Operation(summary = "Activar o desactivar una categoría")
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<ServiceCategoryResponse>> toggle(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(service.toggle(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── DELETE /api/catalog/categories/{id} ───────────────────
    @Operation(
            summary = "Eliminar categoría (soft delete)",
            description = "Desactiva la categoría sin borrar el historial de ventas."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        try {
            service.delete(id);
            return ResponseEntity.ok(ApiResponse.ok("Categoría desactivada", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}