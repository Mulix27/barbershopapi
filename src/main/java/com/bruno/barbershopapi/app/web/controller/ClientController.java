package com.bruno.barbershopapi.app.web.controller;

import com.bruno.barbershopapi.app.service.ClientService;
import com.bruno.barbershopapi.app.web.model.ApiResponse;
import com.bruno.barbershopapi.app.web.model.client.ClientHaircutRequest;
import com.bruno.barbershopapi.app.web.model.client.ClientHaircutResponse;
import com.bruno.barbershopapi.app.web.model.client.ClientRequest;
import com.bruno.barbershopapi.app.web.model.client.ClientResponse;
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
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Clientes", description = "Gestión de clientes de la barbería")
public class ClientController {

    private final ClientService clientService;

    @Operation(summary = "Crear cliente")
    @PostMapping
    public ResponseEntity<ApiResponse<ClientResponse>> create(
            @Valid @RequestBody ClientRequest request) {
        try {
            ClientResponse res = clientService.create(request);
            return ResponseEntity.status(201).body(ApiResponse.ok(res));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "Actualizar cliente")
    @PutMapping("/{clientId}")
    public ResponseEntity<ApiResponse<ClientResponse>> update(
            @PathVariable UUID clientId,
            @Valid @RequestBody ClientRequest request) {
        try {
            ClientResponse res = clientService.update(clientId, request);
            return ResponseEntity.ok(ApiResponse.ok(res));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "Obtener cliente por ID")
    @GetMapping("/{clientId}")
    public ResponseEntity<ApiResponse<ClientResponse>> findById(
            @PathVariable UUID clientId) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(clientService.findById(clientId)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "Listar clientes")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ClientResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(clientService.findAll()));
    }

    @Operation(summary = "Buscar cliente por teléfono")
    @GetMapping("/phone/{phone}")
    public ResponseEntity<ApiResponse<ClientResponse>> findByPhone(
            @PathVariable String phone) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(clientService.findByPhone(phone)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "Buscar clientes por nombre")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ClientResponse>>> searchByName(
            @RequestParam String name) {
        return ResponseEntity.ok(ApiResponse.ok(clientService.searchByName(name)));
    }

    @Operation(summary = "Agregar corte al cliente")
    @PostMapping("/{clientId}/haircuts")
    public ResponseEntity<ApiResponse<ClientHaircutResponse>> addHaircut(
            @PathVariable UUID clientId,
            @Valid @RequestBody ClientHaircutRequest request) {
        try {
            return ResponseEntity.status(201)
                    .body(ApiResponse.ok(clientService.addHaircut(clientId, request)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "Desactivar cliente (soft delete)")
    @DeleteMapping("/{clientId}")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable UUID clientId) {
        try {
            clientService.deactivate(clientId);
            return ResponseEntity.ok(ApiResponse.ok(null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}