package com.bruno.barbershopapi.app.web.model.barbershop;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record BarbershopUpdateRequest(

        @Size(max = 120)
        String name,

        @Size(max = 20)
        String phone,

        @Email
        @Size(max = 120)
        String email,

        String address,

        @Size(max = 80)
        String city,

        @Size(max = 7)
        String primaryColor,

        @Size(max = 80)
        String subdomain,

        Boolean singleBarber
) {}