package com.bruno.barbershopapi.app.web.controller;

import com.bruno.barbershopapi.app.service.ClientService;
import com.bruno.barbershopapi.app.web.model.client.ClientHaircutRequest;
import com.bruno.barbershopapi.app.web.model.client.ClientHaircutResponse;
import com.bruno.barbershopapi.app.web.model.client.ClientRequest;
import com.bruno.barbershopapi.app.web.model.client.ClientResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gestión de clientes de la barbería")
public class ClientController {

    private final ClientService clientService;

    @Operation(summary = "Crear cliente", description = "Registra un nuevo cliente en la barbería")
    @PostMapping
    public ClientResponse create(@Valid @RequestBody ClientRequest request) {
        return clientService.create(request);
    }

    @Operation(summary = "Actualizar cliente", description = "Actualiza la información de un cliente existente")
    @PutMapping("/{clientId}")
    public ClientResponse update(@PathVariable UUID clientId,
                                 @Valid @RequestBody ClientRequest request) {
        return clientService.update(clientId, request);
    }

    @Operation(summary = "Obtener cliente por ID", description = "Obtiene el detalle completo de un cliente con historial de cortes")
    @GetMapping("/{clientId}")
    public ClientResponse findById(@PathVariable UUID clientId) {
        return clientService.findById(clientId);
    }

    @Operation(summary = "Listar clientes", description = "Obtiene todos los clientes activos ordenados por nombre")
    @GetMapping
    public List<ClientResponse> findAll() {
        return clientService.findAll();
    }

    @Operation(summary = "Buscar cliente por teléfono", description = "Busca un cliente usando su número de teléfono")
    @GetMapping("/phone/{phone}")
    public ClientResponse findByPhone(@PathVariable String phone) {
        return clientService.findByPhone(phone);
    }

    @Operation(summary = "Buscar clientes por nombre", description = "Busca clientes por coincidencia parcial en el nombre")
    @GetMapping("/search")
    public List<ClientResponse> searchByName(@RequestParam String name) {
        return clientService.searchByName(name);
    }

    @Operation(summary = "Agregar corte al cliente", description = "Guarda un corte en el historial del cliente")
    @PostMapping("/{clientId}/haircuts")
    public ClientHaircutResponse addHaircut(@PathVariable UUID clientId,
                                            @Valid @RequestBody ClientHaircutRequest request) {
        return clientService.addHaircut(clientId, request);
    }

    @Operation(summary = "Desactivar cliente", description = "Realiza un borrado lógico del cliente (soft delete)")
    @DeleteMapping("/{clientId}")
    public void deactivate(@PathVariable UUID clientId) {
        clientService.deactivate(clientId);
    }
}