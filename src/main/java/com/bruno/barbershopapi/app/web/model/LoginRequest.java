package com.bruno.barbershopapi.app.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// ── Login ─────────────────────────────────────────────────────

@Schema(description = "Credenciales para iniciar sesión")
public record LoginRequest(

        @Schema(description = "Email del usuario", example = "owner@barberia.com")
        @NotBlank(message = "El email es requerido")
        @Email(message = "Email inválido")
        String email,

        @Schema(description = "Contraseña del usuario", example = "miPassword123")
        @NotBlank(message = "La contraseña es requerida")
        String password
) {}