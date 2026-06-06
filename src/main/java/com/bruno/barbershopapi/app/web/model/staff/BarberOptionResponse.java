package com.bruno.barbershopapi.app.web.model.staff;

import java.util.UUID;

public record BarberOptionResponse(
        UUID userId,
        String fullName,
        String specialty,
        String photoUrl,
        String status
) {}