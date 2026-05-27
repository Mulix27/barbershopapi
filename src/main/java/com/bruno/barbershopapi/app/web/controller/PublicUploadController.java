package com.bruno.barbershopapi.app.web.controller;

import com.bruno.barbershopapi.app.service.PhotoService;
import com.bruno.barbershopapi.app.service.QrTokenService;
import com.bruno.barbershopapi.app.web.model.ApiResponse;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/upload")
@RequiredArgsConstructor
@Tag(name = "Público — Upload", description = "Subida de fotos desde celular vía QR. No requiere token de sesión.")
public class PublicUploadController {

    private final QrTokenService qrTokenService;
    private final PhotoService   photoService;

    @Operation(
            summary = "Subir fotos desde celular",
            description = "El celular escanea el QR, abre esta URL y sube las fotos. " +
                    "El token QR valida que la sesión es válida y no ha expirado."
    )
    @PostMapping(
            value = "/{token}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<Map<String, Object>> uploadPhotos(
            @PathVariable String token,
            @RequestPart("files") List<MultipartFile> files,
            @RequestPart(value = "notes", required = false) String notes) {

        // Validar token QR
        if (qrTokenService.isExpired(token)) {
            return ApiResponse.error("El QR ha expirado. Pide al barbero que genere uno nuevo.");
        }

        Claims claims = qrTokenService.validateToken(token);

        if (!"photo".equals(claims.get("type"))) {
            return ApiResponse.error("QR inválido para subida de fotos.");
        }

        // Máximo 5 fotos
        if (files.size() > 5) {
            return ApiResponse.error("Máximo 5 fotos por sesión.");
        }

        UUID clientHaircutId = UUID.fromString(claims.get("clientHaircutId", String.class));
        UUID barbershopId    = UUID.fromString(claims.get("barbershopId",    String.class));

        // Subir cada foto a Cloudinary
        List<String> uploadedUrls = new ArrayList<>();
        List<String> errors       = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                // Reutilizamos el PhotoService existente
                // Necesitamos simular el TenantContext para el servicio
                com.bruno.barbershopapi.util.TenantContext.set(barbershopId);
                var res = photoService.uploadHaircutPhoto(clientHaircutId, file, notes);
                uploadedUrls.add(res.url());
            } catch (Exception e) {
                errors.add(file.getOriginalFilename() + ": " + e.getMessage());
            } finally {
                com.bruno.barbershopapi.util.TenantContext.clear();
            }
        }

        return ApiResponse.ok("Fotos subidas", Map.of(
                "uploaded", uploadedUrls.size(),
                "urls",     uploadedUrls,
                "errors",   errors
        ));
    }

    // Validar token (el celular consulta esto al cargar la página)
    @GetMapping("/validate/{token}")
    public ApiResponse<Map<String, Object>> validateToken(@PathVariable String token) {
        if (qrTokenService.isExpired(token)) {
            return ApiResponse.error("QR expirado");
        }
        Claims claims = qrTokenService.validateToken(token);
        return ApiResponse.ok("Token válido", Map.of(
                "type",    claims.get("type"),
                "valid",   true,
                "expires", claims.getExpiration().toString()
        ));
    }
}