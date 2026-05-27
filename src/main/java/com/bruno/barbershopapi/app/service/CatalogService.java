package com.bruno.barbershopapi.app.service;

import com.bruno.barbershopapi.app.domain.entity.Barbershop;
import com.bruno.barbershopapi.app.domain.entity.HaircutCatalog;
import com.bruno.barbershopapi.app.domain.repository.BarbershopRepository;
import com.bruno.barbershopapi.app.domain.repository.HaircutCatalogRepository;
import com.bruno.barbershopapi.app.web.model.catalog.HaircutCatalogRequest;
import com.bruno.barbershopapi.app.web.model.catalog.HaircutCatalogResponse;
import com.bruno.barbershopapi.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final HaircutCatalogRepository catalogRepository;
    private final BarbershopRepository     barbershopRepository;

    @Transactional
    public HaircutCatalogResponse create(HaircutCatalogRequest req) {
        Barbershop shop = barbershopRepository.findById(TenantContext.get()).orElseThrow();

        HaircutCatalog item = HaircutCatalog.builder()
                .barbershop(shop)
                .name(req.name())
                .type(req.type())
                .description(req.description())
                .price(req.price())
                .durationMin(req.durationMin() != null ? req.durationMin() : 30)
                .isActive(true)
                .build();

        return toResponse(catalogRepository.save(item));
    }

    @Transactional
    public HaircutCatalogResponse update(UUID id, HaircutCatalogRequest req) {
        HaircutCatalog item = findOwned(id);
        item.setName(req.name());
        item.setType(req.type());
        item.setDescription(req.description());
        item.setPrice(req.price());
        if (req.durationMin() != null) item.setDurationMin(req.durationMin());
        return toResponse(catalogRepository.save(item));
    }

    @Transactional(readOnly = true)
    public List<HaircutCatalogResponse> findAll() {
        return catalogRepository
                .findAllByBarbershopIdAndIsActiveTrueOrderByNameAsc(TenantContext.get())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public HaircutCatalogResponse findById(UUID id) {
        return toResponse(findOwned(id));
    }

    @Transactional
    public void toggleActive(UUID id) {
        HaircutCatalog item = findOwned(id);
        item.setIsActive(!item.getIsActive());
        catalogRepository.save(item);
    }

    private HaircutCatalog findOwned(UUID id) {
        HaircutCatalog item = catalogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        if (!item.getBarbershop().getId().equals(TenantContext.get()))
            throw new RuntimeException("Acceso denegado");
        return item;
    }

    private HaircutCatalogResponse toResponse(HaircutCatalog h) {
        return new HaircutCatalogResponse(
                h.getId(), h.getName(), h.getType(),
                h.getDescription(), h.getPrice(),
                h.getDurationMin(), h.getIsActive()
        );
    }
}