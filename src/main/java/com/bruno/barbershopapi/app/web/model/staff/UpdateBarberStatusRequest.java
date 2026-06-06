package com.bruno.barbershopapi.app.web.model.staff;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateBarberStatusRequest(
        @NotBlank
        @Pattern(
                regexp = "active|on_break|inactive",
                message = "Estado debe ser: active, on_break o inactive"
        )
        String status
) {}