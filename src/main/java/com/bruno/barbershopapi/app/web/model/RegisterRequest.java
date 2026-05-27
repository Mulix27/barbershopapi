package com.bruno.barbershopapi.app.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Datos para registrar una nueva barbería en el sistema")
public record RegisterRequest(

        // ── Datos del owner ────────────────────────────────────
        @Schema(description = "Nombre completo del dueño", example = "Carlos Mendoza")
        @NotBlank(message = "El nombre es requerido")
        @Size(max = 120)
        String ownerFullName,

        @Schema(description = "Email del dueño (será su usuario de acceso)", example = "carlos@barberia.com")
        @NotBlank(message = "El email es requerido")
        @Email(message = "Email inválido")
        String ownerEmail,

        @Schema(description = "Contraseña (mínimo 8 caracteres)", example = "miPassword123")
        @NotBlank(message = "La contraseña es requerida")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password,

        // ── Datos de la barbería ───────────────────────────────
        @Schema(description = "Nombre de la barbería", example = "Barbería El Clásico")
        @NotBlank(message = "El nombre de la barbería es requerido")
        @Size(max = 120)
        String barbershopName,

        @Schema(description = "Slug URL amigable (sin espacios ni caracteres especiales)", example = "barberia-el-clasico")
        @NotBlank(message = "El slug es requerido")
        @Size(max = 80)
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
                message = "El slug solo puede contener letras minúsculas, números y guiones")
        String slug,

        @Schema(description = "Teléfono de la barbería", example = "6441234567")
        String phone,

        @Schema(description = "Ciudad", example = "Hermosillo")
        String city,

        @Schema(description = "TRUE si la barbería opera con un solo barbero", example = "true")
        Boolean singleBarber,

        @Schema(description = "ID del plan seleccionado")
        @NotNull(message = "El plan es requerido")
        java.util.UUID planId
) {}