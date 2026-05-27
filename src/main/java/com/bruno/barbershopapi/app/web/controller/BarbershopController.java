package com.bruno.barbershopapi.app.web.controller;

import com.bruno.barbershopapi.app.service.BarbershopService;
import com.bruno.barbershopapi.app.web.model.ApiResponse;
import com.bruno.barbershopapi.app.web.model.barbershop.BarbershopResponse;
import com.bruno.barbershopapi.app.web.model.barbershop.BarbershopUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/barbershop")
@RequiredArgsConstructor
@Tag(name = "Barbershop", description = "Configuración de la barbería autenticada")
public class BarbershopController {

    private final BarbershopService barbershopService;

    @Operation(summary = "Obtener barbería autenticada")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<BarbershopResponse>> getMyBarbershop() {
        return ResponseEntity.ok(
                ApiResponse.ok("Barbería obtenida", barbershopService.getCurrentBarbershop())
        );
    }

    @Operation(summary = "Actualizar barbería autenticada")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<BarbershopResponse>> updateMyBarbershop(
            @Valid @RequestBody BarbershopUpdateRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok("Barbería actualizada", barbershopService.updateCurrentBarbershop(request))
        );
    }

    @Operation(summary = "Actualizar logo de barbería")
    @PatchMapping("/me/logo")
    public ResponseEntity<ApiResponse<BarbershopResponse>> updateLogo(
            @RequestParam String logoUrl
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok("Logo actualizado", barbershopService.updateLogoUrl(logoUrl))
        );
    }

    @PostMapping(value = "/me/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BarbershopResponse>> uploadLogo(
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok("Logo actualizado", barbershopService.uploadLogo(file))
        );
    }
}