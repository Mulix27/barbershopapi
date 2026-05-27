package com.bruno.barbershopapi.app.web.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Envelope estándar para todas las respuestas de la API")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(

        @Schema(description = "Indica si la operación fue exitosa")
        boolean success,

        @Schema(description = "Mensaje descriptivo del resultado")
        String message,

        @Schema(description = "Datos de la respuesta")
        T data,

        @Schema(description = "Timestamp de la respuesta")
        OffsetDateTime timestamp
) {
    // Constructores de conveniencia
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", data, OffsetDateTime.now());
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, OffsetDateTime.now());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, OffsetDateTime.now());
    }
}