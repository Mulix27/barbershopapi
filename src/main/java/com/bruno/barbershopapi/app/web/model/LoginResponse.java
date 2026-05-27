package com.bruno.barbershopapi.app.web.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Respuesta de autenticación con token JWT")
public record LoginResponse(

        @Schema(description = "Token JWT para incluir en el header Authorization: Bearer <token>")
        String token,

        @Schema(description = "Tipo de token", example = "Bearer")
        String tokenType,

        @Schema(description = "ID del usuario autenticado")
        UUID userId,

        @Schema(description = "Nombre completo del usuario")
        String fullName,

        @Schema(description = "Email del usuario")
        String email,

        @Schema(description = "Rol del usuario en la barbería", example = "owner")
        String role,

        @Schema(description = "ID de la barbería")
        UUID barbershopId,

        @Schema(description = "Nombre de la barbería")
        String barbershopName,

        @Schema(description = "URL del logo de la barbería")
        String logoUrl,

        @Schema(description = "TRUE si la barbería opera con un solo barbero")
        Boolean singleBarber
) {}