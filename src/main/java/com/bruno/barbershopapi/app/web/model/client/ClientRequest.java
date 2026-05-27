package com.bruno.barbershopapi.app.web.model.client;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Datos para registrar o actualizar un cliente")
public record ClientRequest(

        @Schema(description = "Nombre completo", example = "Juan Pérez")
        @NotBlank(message = "El nombre es requerido")
        @Size(max = 120)
        String fullName,

        @Schema(description = "Teléfono — identificador principal de búsqueda", example = "6441234567")
        @NotBlank(message = "El teléfono es requerido")
        @Size(max = 20)
        String phone,

        @Schema(description = "Email del cliente", example = "juan@mail.com")
        @Email(message = "Email inválido")
        String email,

        @Schema(description = "Fecha de nacimiento", example = "1995-03-15")
        LocalDate birthDate,

        @Schema(description = "Notas generales del cliente", example = "Alérgico a ciertos productos")
        String notes
) {
}