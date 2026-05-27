package com.bruno.barbershopapi.app.web.controller;

import com.bruno.barbershopapi.app.service.QrTokenService;
import com.bruno.barbershopapi.app.web.model.ApiResponse;
import com.bruno.barbershopapi.util.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "QR", description = "Generación de QR para subir fotos y agenda")
public class QrController {

    private final QrTokenService qrTokenService;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Operation(summary = "Generar QR para subir fotos de un corte")
    @PostMapping("/photo/{clientHaircutId}")
    public ApiResponse<Map<String, String>> generatePhotoQr(
            @PathVariable UUID clientHaircutId) {

        UUID barbershopId = TenantContext.get();
        String token = qrTokenService.generatePhotoToken(clientHaircutId, barbershopId);
        String url   = frontendUrl + "/upload/" + token;

        return ApiResponse.ok("QR generado", Map.of(
                "token",          token,
                "url",            url,
                "expiresInMin",   "15",
                "type",           "photo"
        ));
    }

    @Operation(summary = "Generar QR para agenda (el cliente escanea y agenda)")
    @PostMapping("/agenda")
    public ApiResponse<Map<String, String>> generateAgendaQr() {
        UUID barbershopId = TenantContext.get();
        String token = qrTokenService.generateAgendaToken(barbershopId);
        String url   = frontendUrl + "/b/" + barbershopId + "?qr=" + token;

        return ApiResponse.ok("QR generado", Map.of(
                "token",        token,
                "url",          url,
                "expiresInMin", "60",
                "type",         "agenda"
        ));
    }
}