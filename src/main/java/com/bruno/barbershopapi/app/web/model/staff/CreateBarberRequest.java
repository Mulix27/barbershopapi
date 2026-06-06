package com.bruno.barbershopapi.app.web.model.staff;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBarberRequest(
        @NotBlank(message = "El nombre completo es requerido")
        String fullName,

        @NotBlank(message = "El email es requerido")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "La contraseña es requerida")
        @Size(min = 6, message = "Mínimo 6 caracteres")
        String password,

        String specialty,
        String bio
) {}