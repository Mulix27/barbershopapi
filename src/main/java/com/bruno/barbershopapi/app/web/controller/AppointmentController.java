package com.bruno.barbershopapi.app.web.controller;

import com.bruno.barbershopapi.app.facade.AppointmentFacade;
import com.bruno.barbershopapi.app.web.model.ApiResponse;
import com.bruno.barbershopapi.app.web.model.appointment.appointment.AppointmentRequest;
import com.bruno.barbershopapi.app.web.model.appointment.appointment.AppointmentResponse;
import com.bruno.barbershopapi.app.web.model.appointment.appointment.AssignBarberRequest;
import com.bruno.barbershopapi.app.web.model.appointment.availability.DayAvailabilityResponse;
import com.bruno.barbershopapi.app.web.model.appointment.blocked.BlockedTimeRequest;
import com.bruno.barbershopapi.app.web.model.appointment.schedule.BarberScheduleRequest;
import com.bruno.barbershopapi.app.web.model.appointment.schedule.BarberScheduleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentFacade appointmentFacade;

    // ══════════════════════════════════════════════════════════
    //  ENDPOINTS PÚBLICOS — el cliente agenda desde la web
    //  Base: /api/public/barbershops/{barbershopId}/appointments
    // ══════════════════════════════════════════════════════════

    @Tag(name = "Público — Agenda", description = "Endpoints públicos para que el cliente vea disponibilidad y agende citas. No requieren token.")
    @Operation(
            summary = "Ver slots disponibles de una barbería",
            description = "Retorna todos los slots del día indicando cuáles están disponibles. " +
                    "Usado por la página pública de la barbería para mostrar el calendario."
    )
    @GetMapping("/api/public/barbershops/{barbershopId}/availability")
    public ResponseEntity<ApiResponse<DayAvailabilityResponse>> getAvailability(
            @PathVariable UUID barbershopId,
            @Parameter(description = "Fecha a consultar (YYYY-MM-DD)", example = "2025-06-15")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        ApiResponse<DayAvailabilityResponse> res =
                appointmentFacade.getAvailability(barbershopId, date);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    @Tag(name = "Público — Agenda")
    @Operation(
            summary = "Agendar una cita",
            description = "El cliente elige un slot disponible y agenda su cita. " +
                    "El sistema asigna automáticamente al barbero disponible."
    )
    @PostMapping("/api/public/barbershops/{barbershopId}/appointments")
    public ResponseEntity<ApiResponse<AppointmentResponse>> createPublic(
            @PathVariable UUID barbershopId,
            @Valid @RequestBody AppointmentRequest req) {

        ApiResponse<AppointmentResponse> res =
                appointmentFacade.create(barbershopId, req);
        return ResponseEntity.status(res.success() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST).body(res);
    }

    // ══════════════════════════════════════════════════════════
    //  ENDPOINTS PRIVADOS — dashboard de la barbería
    //  Base: /api/appointments
    // ══════════════════════════════════════════════════════════

    @Tag(name = "Agenda", description = "Gestión de citas desde el dashboard. Requieren token JWT.")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Citas del día",
            description = "Lista todas las citas de la barbería en una fecha. Vista del encargado."
    )
    @GetMapping("/api/appointments")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> findByDate(
            @Parameter(description = "Fecha (YYYY-MM-DD). Default: hoy")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate target = date != null ? date : LocalDate.now();
        ApiResponse<List<AppointmentResponse>> res = appointmentFacade.findByDate(target);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    @Tag(name = "Agenda")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Citas pendientes de asignación", description = "Lista las citas que aún no tienen barbero asignado.")
    @GetMapping("/api/appointments/pending")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> findPending() {
        ApiResponse<List<AppointmentResponse>> res = appointmentFacade.findPending();
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    @Tag(name = "Agenda")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Mi agenda del día",
            description = "El barbero ve solo SUS citas del día. Usa el token para identificar al barbero."
    )
    @GetMapping("/api/appointments/my-agenda")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> myAgenda(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate target = date != null ? date : LocalDate.now();
        ApiResponse<List<AppointmentResponse>> res = appointmentFacade.findMyAgenda(target);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    @Tag(name = "Agenda")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Historial de citas de un cliente")
    @GetMapping("/api/appointments/client/{clientId}")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> findByClient(
            @PathVariable UUID clientId) {
        ApiResponse<List<AppointmentResponse>> res = appointmentFacade.findByClient(clientId);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    @Tag(name = "Agenda")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Asignar barbero a una cita",
            description = "El encargado asigna un barbero a una cita pendiente. Valida que no haya conflicto de horario."
    )
    @PatchMapping("/api/appointments/{id}/assign")
    public ResponseEntity<ApiResponse<AppointmentResponse>> assignBarber(
            @PathVariable UUID id,
            @Valid @RequestBody AssignBarberRequest req) {
        ApiResponse<AppointmentResponse> res = appointmentFacade.assignBarber(id, req);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    @Tag(name = "Agenda")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Cambiar estado de una cita",
            description = "Estados válidos: confirmed → in_progress → completed | cancelled | no_show"
    )
    @PatchMapping("/api/appointments/{id}/status")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateStatus(
            @PathVariable UUID id,
            @Parameter(description = "Nuevo estado", example = "in_progress")
            @RequestParam String status) {
        ApiResponse<AppointmentResponse> res = appointmentFacade.updateStatus(id, status);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    // ══════════════════════════════════════════════════════════
    //  HORARIOS DE BARBEROS
    // ══════════════════════════════════════════════════════════

    @Tag(name = "Agenda")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Guardar horario de un barbero",
            description = "Crea o actualiza el horario de trabajo de un barbero para un día de la semana."
    )
    @PostMapping("/api/barbers/{userId}/schedule")
    public ResponseEntity<ApiResponse<BarberScheduleResponse>> saveSchedule(
            @PathVariable UUID userId,
            @Valid @RequestBody BarberScheduleRequest req) {
        ApiResponse<BarberScheduleResponse> res = appointmentFacade.saveSchedule(userId, req);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    @Tag(name = "Agenda")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ver horarios de un barbero")
    @GetMapping("/api/barbers/{userId}/schedule")
    public ResponseEntity<ApiResponse<List<BarberScheduleResponse>>> getSchedules(
            @PathVariable UUID userId) {
        ApiResponse<List<BarberScheduleResponse>> res = appointmentFacade.getSchedules(userId);
        return ResponseEntity.status(res.success() ? 200 : 400).body(res);
    }

    @Tag(name = "Agenda")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Bloquear tiempo en la agenda",
            description = "Bloquea un día completo o un rango de horas para un barbero. " +
                    "Ej: vacaciones, comida, cita médica."
    )
    @PostMapping("/api/barbers/{userId}/blocked-times")
    public ResponseEntity<ApiResponse<Void>> addBlockedTime(
            @PathVariable UUID userId,
            @Valid @RequestBody BlockedTimeRequest req) {
        ApiResponse<Void> res = appointmentFacade.addBlockedTime(userId, req);
        return ResponseEntity.status(res.success() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST).body(res);
    }
}