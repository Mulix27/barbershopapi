package com.bruno.barbershopapi.app.web.model.appointment.catalog;

import java.math.BigDecimal;
import java.util.UUID;

public record AppointmentServiceOption(
        UUID categoryId,
        String categoryName,
        String categoryIcon,
        String pricingMode,
        UUID variantId,
        String variantName,
        BigDecimal price,
        Integer durationMin,
        String displayName
) {}