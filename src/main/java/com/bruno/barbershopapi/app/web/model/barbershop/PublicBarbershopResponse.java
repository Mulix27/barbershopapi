package com.bruno.barbershopapi.app.web.model.barbershop;

import java.util.UUID;

public record PublicBarbershopResponse(
        UUID id,
        String name,
        String slug,
        String logoUrl,
        String city,
        String address,
        String primaryColor
) {}