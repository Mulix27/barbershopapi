package com.bruno.barbershopapi.app.web.model.staff;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record BarberResponse(
        UUID profileId,
        UUID userId,
        String fullName,
        String email,
        String specialty,
        String bio,
        String photoUrl,
        String status,
        BigDecimal rating,
        Integer totalCuts,
        boolean isActive,
        OffsetDateTime createdAt
) {}