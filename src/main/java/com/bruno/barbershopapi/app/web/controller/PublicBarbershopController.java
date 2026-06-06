package com.bruno.barbershopapi.app.web.controller;

import com.bruno.barbershopapi.app.domain.entity.Barbershop;
import com.bruno.barbershopapi.app.domain.repository.BarbershopRepository;
import com.bruno.barbershopapi.app.web.model.ApiResponse;
import com.bruno.barbershopapi.app.web.model.barbershop.PublicBarbershopResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/public/barbershops")
@RequiredArgsConstructor
@Tag(
        name = "Público — Barbería",
        description = "Datos públicos de la barbería para reservas sin token."
)
public class PublicBarbershopController {

    private final BarbershopRepository barbershopRepository;

    @Operation(
            summary = "Obtener barbería pública",
            description = "Retorna nombre, logo y datos básicos de la barbería para la página pública de reservas."
    )
    @GetMapping("/{barbershopId}")
    public ResponseEntity<ApiResponse<PublicBarbershopResponse>> getPublicBarbershop(
            @PathVariable UUID barbershopId
    ) {
        Barbershop barbershop = barbershopRepository.findById(barbershopId)
                .orElseThrow(() -> new RuntimeException("Barbería no encontrada"));

        PublicBarbershopResponse response = new PublicBarbershopResponse(
                barbershop.getId(),
                barbershop.getName(),
                barbershop.getSlug(),
                barbershop.getLogoUrl(),
                barbershop.getCity(),
                barbershop.getAddress(),
                barbershop.getPrimaryColor()
        );

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}