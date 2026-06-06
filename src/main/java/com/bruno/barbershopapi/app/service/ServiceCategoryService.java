package com.bruno.barbershopapi.app.service;

import com.bruno.barbershopapi.app.domain.entity.Barbershop;
import com.bruno.barbershopapi.app.domain.entity.ServiceCategory;
import com.bruno.barbershopapi.app.domain.entity.ServiceVariant;

import com.bruno.barbershopapi.app.domain.repository.BarbershopRepository;
import com.bruno.barbershopapi.app.domain.repository.ServiceCategoryRepository;


import com.bruno.barbershopapi.app.web.model.catalog.*;
import com.bruno.barbershopapi.util.TenantContext;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceCategoryService {

    private final ServiceCategoryRepository categoryRepo;
    private final BarbershopRepository barbershopRepo;

    // ─────────────────────────────────────────
    // LISTAR
    // ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ServiceCategoryResponse> getAll(boolean onlyActive) {

        UUID shopId = TenantContext.get();

        List<ServiceCategory> categories = onlyActive
                ? categoryRepo.findAllActiveByShop(shopId)
                : categoryRepo.findAllByShop(shopId);

        return categories.stream()
                .map(this::toResponse)
                .toList();
    }

    // Selector plano para citas / ventas
    @Transactional(readOnly = true)
    public List<ServiceSelectOption> getSelectOptions() {

        UUID shopId = TenantContext.get();

        List<ServiceCategory> categories =
                categoryRepo.findAllActiveByShop(shopId);

        List<ServiceSelectOption> options = new ArrayList<>();

        for (ServiceCategory category : categories) {

            // Precio fijo
            if ("fixed".equals(category.getPricingMode())) {

                options.add(
                        new ServiceSelectOption(
                                category.getId(),
                                category.getName(),
                                category.getIcon(),
                                "fixed",
                                null,
                                null,
                                category.getBasePrice(),
                                category.getBaseDuration(),
                                category.getName()
                        )
                );

                continue;
            }

            // Variantes
            category.getVariants()
                    .stream()
                    .filter(ServiceVariant::isActive)
                    .forEach(variant -> {

                        options.add(
                                new ServiceSelectOption(
                                        category.getId(),
                                        category.getName(),
                                        category.getIcon(),
                                        "variants",
                                        variant.getId(),
                                        variant.getName(),
                                        variant.getPrice(),
                                        variant.getDurationMin(),
                                        category.getName() + " — " + variant.getName()
                                )
                        );
                    });
        }

        return options;
    }

    @Transactional(readOnly = true)
    public List<ServiceSelectOption> getPublicSelectOptions(UUID barbershopId) {
        List<ServiceCategory> categories =
                categoryRepo.findAllActiveByShop(barbershopId);

        List<ServiceSelectOption> options = new ArrayList<>();

        for (ServiceCategory category : categories) {

            if ("fixed".equals(category.getPricingMode())) {
                options.add(
                        new ServiceSelectOption(
                                category.getId(),
                                category.getName(),
                                category.getIcon(),
                                "fixed",
                                null,
                                null,
                                category.getBasePrice(),
                                category.getBaseDuration(),
                                category.getName()
                        )
                );

                continue;
            }

            category.getVariants()
                    .stream()
                    .filter(ServiceVariant::isActive)
                    .forEach(variant -> {
                        options.add(
                                new ServiceSelectOption(
                                        category.getId(),
                                        category.getName(),
                                        category.getIcon(),
                                        "variants",
                                        variant.getId(),
                                        variant.getName(),
                                        variant.getPrice(),
                                        variant.getDurationMin(),
                                        category.getName() + " — " + variant.getName()
                                )
                        );
                    });
        }

        return options;
    }

    // ─────────────────────────────────────────
    // CREAR
    // ─────────────────────────────────────────

    @Transactional
    public ServiceCategoryResponse create(ServiceCategoryRequest req) {

        UUID shopId = TenantContext.get();

        Barbershop shop = barbershopRepo.findById(shopId)
                .orElseThrow(() ->
                        new RuntimeException("Barbería no encontrada"));

        validateRequest(req);

        ServiceCategory category = ServiceCategory.builder()
                .barbershop(shop)
                .name(req.name())
                .icon(req.icon() != null ? req.icon() : "pi pi-scissors")
                .pricingMode(req.pricingMode())
                .basePrice(req.basePrice())
                .baseDuration(req.baseDuration())
                .sortOrder(req.sortOrder() != null ? req.sortOrder() : 0)
                .isActive(true)
                .build();

        // Variantes
        if ("variants".equals(req.pricingMode())
                && req.variants() != null) {

            for (int i = 0; i < req.variants().size(); i++) {

                category.addVariant(
                        buildVariant(req.variants().get(i), i)
                );
            }
        }

        return toResponse(categoryRepo.save(category));
    }

    // ─────────────────────────────────────────
    // ACTUALIZAR
    // ─────────────────────────────────────────

    @Transactional
    public ServiceCategoryResponse update(
            UUID id,
            ServiceCategoryRequest req
    ) {

        UUID shopId = TenantContext.get();

        ServiceCategory category =
                categoryRepo.findByIdAndBarbershopId(id, shopId)
                        .orElseThrow(() ->
                                new RuntimeException("Servicio no encontrado"));

        validateRequest(req);

        category.setName(req.name());
        category.setPricingMode(req.pricingMode());
        category.setBasePrice(req.basePrice());
        category.setBaseDuration(req.baseDuration());

        if (req.icon() != null) {
            category.setIcon(req.icon());
        }

        if (req.sortOrder() != null) {
            category.setSortOrder(req.sortOrder());
        }

        // Limpiar variantes
        category.clearVariants();

        // Recrear variantes
        if ("variants".equals(req.pricingMode())
                && req.variants() != null) {

            for (int i = 0; i < req.variants().size(); i++) {

                category.addVariant(
                        buildVariant(req.variants().get(i), i)
                );
            }
        }

        return toResponse(categoryRepo.save(category));
    }

    // ─────────────────────────────────────────
    // TOGGLE
    // ─────────────────────────────────────────

    @Transactional
    public ServiceCategoryResponse toggle(UUID id) {

        UUID shopId = TenantContext.get();

        ServiceCategory category =
                categoryRepo.findByIdAndBarbershopId(id, shopId)
                        .orElseThrow(() ->
                                new RuntimeException("Servicio no encontrado"));

        category.setActive(!category.isActive());

        return toResponse(categoryRepo.save(category));
    }

    // ─────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────

    @Transactional
    public void delete(UUID id) {

        UUID shopId = TenantContext.get();

        ServiceCategory category =
                categoryRepo.findByIdAndBarbershopId(id, shopId)
                        .orElseThrow(() ->
                                new RuntimeException("Servicio no encontrado"));

        category.setActive(false);

        categoryRepo.save(category);
    }

    // ─────────────────────────────────────────
    // VALIDACIONES
    // ─────────────────────────────────────────

    private void validateRequest(ServiceCategoryRequest req) {

        if ("fixed".equals(req.pricingMode())) {

            if (req.basePrice() == null) {
                throw new RuntimeException(
                        "El precio base es requerido"
                );
            }

            if (req.baseDuration() == null) {
                throw new RuntimeException(
                        "La duración base es requerida"
                );
            }
        }

        if ("variants".equals(req.pricingMode())) {

            if (req.variants() == null
                    || req.variants().isEmpty()) {

                throw new RuntimeException(
                        "Debes agregar al menos una variante"
                );
            }
        }
    }

    // ─────────────────────────────────────────
    // BUILD VARIANT
    // ─────────────────────────────────────────

    private ServiceVariant buildVariant(
            ServiceVariantRequest req,
            int order
    ) {

        return ServiceVariant.builder()
                .name(req.name())
                .description(req.description())
                .price(req.price())
                .durationMin(req.durationMin())
                .sortOrder(
                        req.sortOrder() != null
                                ? req.sortOrder()
                                : order
                )
                .isActive(
                        req.isActive() != null
                                ? req.isActive()
                                : true
                )
                .build();
    }

    // ─────────────────────────────────────────
    // MAPPERS
    // ─────────────────────────────────────────

    private ServiceCategoryResponse toResponse(
            ServiceCategory category
    ) {

        List<ServiceVariantResponse> variants =
                category.getVariants()
                        .stream()
                        .map(variant ->
                                new ServiceVariantResponse(
                                        variant.getId(),
                                        variant.getName(),
                                        variant.getDescription(),
                                        variant.getPrice(),
                                        variant.getDurationMin(),
                                        variant.getSortOrder(),
                                        variant.isActive(),
                                        variant.getCreatedAt()
                                )
                        ).toList();

        return new ServiceCategoryResponse(
                category.getId(),
                category.getName(),
                category.getIcon(),
                category.getPricingMode(),
                category.getBasePrice(),
                category.getBaseDuration(),
                category.getSortOrder(),
                category.isActive(),
                category.getCreatedAt(),
                variants
        );
    }
}