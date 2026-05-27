package com.bruno.barbershopapi.app.web.model.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PdfReportRequest(

        @NotBlank
        @Pattern(regexp = "today|week|month|custom")
        String period,

        String from,
        String to

) {}