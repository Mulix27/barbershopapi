package com.bruno.barbershopapi.app.facade;

import com.bruno.barbershopapi.app.service.CatalogService;
import com.bruno.barbershopapi.app.web.model.ApiResponse;
import com.bruno.barbershopapi.app.web.model.catalog.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogFacade {

    private final CatalogService catalogService;

    public ApiResponse<HaircutCatalogResponse> create(HaircutCatalogRequest req) {
        try {
            return ApiResponse.ok("Servicio creado", catalogService.create(req));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<HaircutCatalogResponse> update(UUID id, HaircutCatalogRequest req) {
        try {
            return ApiResponse.ok("Servicio actualizado", catalogService.update(id, req));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<List<HaircutCatalogResponse>> findAll() {
        try {
            return ApiResponse.ok(catalogService.findAll());
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<HaircutCatalogResponse> findById(UUID id) {
        try {
            return ApiResponse.ok(catalogService.findById(id));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<Void> toggleActive(UUID id) {
        try {
            catalogService.toggleActive(id);
            return ApiResponse.ok("Estado actualizado", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}