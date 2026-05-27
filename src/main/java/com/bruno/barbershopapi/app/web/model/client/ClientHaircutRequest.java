package com.bruno.barbershopapi.app.web.model.client;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para guardar un corte en el perfil del cliente")
public record ClientHaircutRequest(

        @Schema(description = "Tipo de corte", example = "hair", allowableValues = {"hair", "beard", "combo"})
        @NotBlank(message = "El tipo es requerido")
        String type,

        @Schema(description = "Nombre del corte", example = "Fade bajo con raya")
        @NotBlank(message = "El nombre es requerido")
        @Size(max = 120)
        String name,

        @Schema(description = "Descripción detallada para el barbero")
        String description,

        @Schema(description = "Marcar como corte preferido")
        Boolean isPreferred
) {
}