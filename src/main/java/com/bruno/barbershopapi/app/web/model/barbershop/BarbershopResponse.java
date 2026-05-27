package com.bruno.barbershopapi.app.web.model.barbershop;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BarbershopResponse(
        UUID id,
        String name,
        String slug,
        String phone,
        String email,
        String address,
        String city,
        String logoUrl,
        String primaryColor,
        String subdomain,
        Boolean isActive,
        Boolean singleBarber,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}