package com.bruno.barbershopapi.app.web.controller;

import com.bruno.barbershopapi.app.service.ServiceCategoryService;
import com.bruno.barbershopapi.app.web.model.ApiResponse;
import com.bruno.barbershopapi.app.web.model.catalog.ServiceSelectOption;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/barbershops/{barbershopId}")
@RequiredArgsConstructor
@Tag(
        name = "Público — Catálogo",
        description = "Servicios públicos para que el cliente pueda reservar sin token."
)
public class PublicCatalogController {

    private final ServiceCategoryService serviceCategoryService;

    @Operation(
            summary = "Listar servicios públicos",
            description = "Lista servicios activos de la barbería para la página pública de reservas."
    )
    @GetMapping("/services")
    public ResponseEntity<ApiResponse<List<ServiceSelectOption>>> getServices(
            @PathVariable UUID barbershopId
    ) {
        List<ServiceSelectOption> services =
                serviceCategoryService.getPublicSelectOptions(barbershopId);

        return ResponseEntity.ok(ApiResponse.ok(services));
    }
}