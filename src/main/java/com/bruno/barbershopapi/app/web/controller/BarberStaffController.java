package com.bruno.barbershopapi.app.web.controller;

import com.bruno.barbershopapi.app.service.BarberStaffService;
import com.bruno.barbershopapi.app.web.model.ApiResponse;
import com.bruno.barbershopapi.app.web.model.staff.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/barbershop/staff")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Barber Staff", description = "Gestión de barberos: perfiles, estados y fotos")
public class BarberStaffController {

    private final BarberStaffService staffService;

    // ── GET /api/barbershop/staff ──────────────────────────────
    @Operation(summary = "Listar todos los barberos de la barbería")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BarberResponse>>> getAll() {
        var res = staffService.getAll();
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    // ── GET /api/barbershop/staff/options ──────────────────────
    @Operation(summary = "Opciones simplificadas para dropdowns (solo activos)")
    @GetMapping("/options")
    public ResponseEntity<ApiResponse<List<BarberOptionResponse>>> getOptions() {
        var res = staffService.getOptions();
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    // ── GET /api/barbershop/staff/{id} ─────────────────────────
    @Operation(summary = "Obtener un barbero por su profileId")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BarberResponse>> getOne(@PathVariable UUID id) {
        var res = staffService.getOne(id);
        return ResponseEntity.status(res.success() ? 200 : 404).body(res);
    }

    // ── POST /api/barbershop/staff ─────────────────────────────
    @Operation(
            summary = "Crear nuevo barbero",
            description = "Crea un usuario con rol 'barber' y su perfil extendido."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<BarberResponse>> create(
            @Valid @RequestBody CreateBarberRequest req) {
        var res = staffService.create(req);
        return ResponseEntity.status(res.success() ? 201 : 400).body(res);
    }

    // ── PUT /api/barbershop/staff/{id} ─────────────────────────
    @Operation(summary = "Actualizar perfil del barbero (nombre, especialidad, bio)")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BarberResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBarberRequest req) {
        var res = staffService.update(id, req);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    // ── PATCH /api/barbershop/staff/{id}/status ────────────────
    @Operation(
            summary = "Cambiar estado del barbero",
            description = "Estados: active | on_break | inactive. " +
                    "Inactive también desactiva el usuario en el sistema."
    )
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<BarberResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBarberStatusRequest req) {
        var res = staffService.updateStatus(id, req);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    // ── POST /api/barbershop/staff/{id}/photo ──────────────────
    @Operation(
            summary = "Subir o reemplazar foto de perfil",
            description = "Sube la foto a Cloudinary. Si ya tenía foto, la reemplaza automáticamente."
    )
    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BarberResponse>> uploadPhoto(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {
        var res = staffService.uploadPhoto(id, file);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    // ── DELETE /api/barbershop/staff/{id}/photo ────────────────
    @Operation(summary = "Eliminar foto de perfil del barbero")
    @DeleteMapping("/{id}/photo")
    public ResponseEntity<ApiResponse<BarberResponse>> deletePhoto(@PathVariable UUID id) {
        var res = staffService.deletePhoto(id);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    // ── DELETE /api/barbershop/staff/{id} ─────────────────────
    @Operation(
            summary = "Eliminar barbero",
            description = "Soft delete: desactiva el usuario y el perfil. " +
                    "Preserva el historial de citas y ventas."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        var res = staffService.delete(id);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }
}