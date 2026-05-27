package com.bruno.barbershopapi.app.service;

import com.bruno.barbershopapi.app.domain.entity.*;
import com.bruno.barbershopapi.app.domain.repository.*;
import com.bruno.barbershopapi.app.web.model.client.*;
import com.bruno.barbershopapi.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository        clientRepository;
    private final ClientHaircutRepository clientHaircutRepository;
    private final BarbershopRepository    barbershopRepository;
    private final HaircutPhotoRepository haircutPhotoRepository;

    // ── Crear cliente ──────────────────────────────────────────

    @Transactional
    public ClientResponse create(ClientRequest req) {
        UUID shopId = TenantContext.get();

        if (clientRepository.existsByBarbershopIdAndPhone(shopId, req.phone())) {
            throw new RuntimeException("Ya existe un cliente con el teléfono " + req.phone());
        }

        Barbershop shop = barbershopRepository.findById(shopId).orElseThrow();

        Client client = Client.builder()
                .barbershop(shop)
                .fullName(req.fullName())
                .phone(req.phone())
                .email(req.email())
                .birthDate(req.birthDate())
                .notes(req.notes())
                .isActive(true)
                .build();

        return toResponse(clientRepository.save(client), List.of());
    }

    // ── Actualizar cliente ─────────────────────────────────────

    @Transactional
    public ClientResponse update(UUID clientId, ClientRequest req) {
        Client client = findOwned(clientId);

        // Si cambió el teléfono, validar que no esté en uso
        if (!client.getPhone().equals(req.phone()) &&
                clientRepository.existsByBarbershopIdAndPhone(TenantContext.get(), req.phone())) {
            throw new RuntimeException("El teléfono " + req.phone() + " ya está en uso");
        }

        client.setFullName(req.fullName());
        client.setPhone(req.phone());
        client.setEmail(req.email());
        client.setBirthDate(req.birthDate());
        client.setNotes(req.notes());

        return toResponse(clientRepository.save(client), getHaircuts(clientId));
    }

    // ── Buscar por teléfono ────────────────────────────────────

    @Transactional(readOnly = true)
    public ClientResponse findByPhone(String phone) {
        Client client = clientRepository
                .findByBarbershopIdAndPhone(TenantContext.get(), phone)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con teléfono: " + phone));
        return toResponse(client, getHaircuts(client.getId()));
    }

    // ── Buscar por nombre ──────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ClientResponse> searchByName(String name) {
        return clientRepository
                .findByBarbershopIdAndFullNameContainingIgnoreCaseAndIsActiveTrue(
                        TenantContext.get(), name)
                .stream()
                .map(c -> toResponse(c, List.of()))
                .toList();
    }

    // ── Listar todos ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ClientResponse> findAll() {
        return clientRepository
                .findAllByBarbershopIdAndIsActiveTrueOrderByFullNameAsc(TenantContext.get())
                .stream()
                .map(c -> toResponse(c, List.of()))
                .toList();
    }

    // ── Detalle con historial de cortes ────────────────────────

    @Transactional(readOnly = true)
    public ClientResponse findById(UUID clientId) {
        Client client = findOwned(clientId);
        return toResponse(client, getHaircuts(clientId));
    }

    // ── Guardar corte al cliente ───────────────────────────────

    @Transactional
    public ClientHaircutResponse addHaircut(UUID clientId, ClientHaircutRequest req) {
        Client client = findOwned(clientId);
        Barbershop shop = barbershopRepository.findById(TenantContext.get()).orElseThrow();

        // Si es preferido, quitar el flag de los demás
        if (Boolean.TRUE.equals(req.isPreferred())) {
            clientHaircutRepository.findAllByClientId(clientId)
                    .forEach(h -> h.setIsPreferred(false));
        }

        ClientHaircut haircut = ClientHaircut.builder()
                .client(client)
                .barbershop(shop)
                .type(req.type().toLowerCase())
                .name(req.name())
                .description(req.description())
                .isPreferred(Boolean.TRUE.equals(req.isPreferred()))
                .build();

        return toHaircutResponse(clientHaircutRepository.save(haircut));
    }

    // ── Desactivar cliente (soft delete) ──────────────────────

    @Transactional
    public void deactivate(UUID clientId) {
        Client client = findOwned(clientId);
        client.setIsActive(false);
        clientRepository.save(client);
    }

    // ── Helpers ───────────────────────────────────────────────

    private Client findOwned(UUID clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        if (!client.getBarbershop().getId().equals(TenantContext.get())) {
            throw new RuntimeException("Acceso denegado");
        }
        return client;
    }

    private List<ClientHaircutResponse> getHaircuts(UUID clientId) {
        return clientHaircutRepository
                .findAllByClientIdOrderByIsPreferredDescCreatedAtDesc(clientId)
                .stream()
                .map(this::toHaircutResponse)
                .toList();
    }

    private ClientResponse toResponse(Client c, List<ClientHaircutResponse> haircuts) {
        return new ClientResponse(
                c.getId(), c.getFullName(), c.getPhone(), c.getEmail(),
                c.getBirthDate(), c.getNotes(), c.getIsActive(), c.getCreatedAt(),
                haircuts
        );
    }

    private ClientHaircutResponse toHaircutResponse(ClientHaircut h) {
        return new ClientHaircutResponse(
                h.getId(),
                h.getType(),
                h.getName(),
                h.getDescription(),
                h.getIsPreferred(),
                haircutPhotoRepository
                        .findAllByClientHaircutIdOrderByTakenAtDesc(h.getId())
                        .stream()
                        .map(this::toPhotoResponse)
                        .toList(),
                h.getCreatedAt()
        );
    }

    private HaircutPhotoResponse toPhotoResponse(HaircutPhoto p) {
        return new HaircutPhotoResponse(
                p.getId(),
                p.getUrl(),
                p.getTakenAt(),
                p.getNotes()
        );
    }
}