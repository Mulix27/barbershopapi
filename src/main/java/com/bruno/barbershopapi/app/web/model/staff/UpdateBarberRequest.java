package com.bruno.barbershopapi.app.web.model.staff;

import jakarta.validation.constraints.NotBlank;

public record UpdateBarberRequest(
        @NotBlank(message = "El nombre completo es requerido")
        String fullName,

        String specialty,
        String bio
) {}