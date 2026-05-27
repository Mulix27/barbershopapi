package com.bruno.barbershopapi.app.web.model.appointment.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Asignar barbero a una cita pendiente")
public record AssignBarberRequest(

        @NotNull
        UUID barberId
) {}