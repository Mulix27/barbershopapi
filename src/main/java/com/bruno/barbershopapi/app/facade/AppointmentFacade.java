package com.bruno.barbershopapi.app.facade;

import com.bruno.barbershopapi.app.service.AppointmentService;
import com.bruno.barbershopapi.app.web.model.ApiResponse;
import com.bruno.barbershopapi.app.web.model.appointment.appointment.AppointmentRequest;
import com.bruno.barbershopapi.app.web.model.appointment.appointment.AppointmentResponse;
import com.bruno.barbershopapi.app.web.model.appointment.appointment.AssignBarberRequest;
import com.bruno.barbershopapi.app.web.model.appointment.availability.DayAvailabilityResponse;
import com.bruno.barbershopapi.app.web.model.appointment.blocked.BlockedTimeRequest;
import com.bruno.barbershopapi.app.web.model.appointment.schedule.BarberScheduleRequest;
import com.bruno.barbershopapi.app.web.model.appointment.schedule.BarberScheduleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentFacade {

    private final AppointmentService appointmentService;

    public ApiResponse<DayAvailabilityResponse> getAvailability(
            UUID barbershopId,
            LocalDate date,
            Integer durationMin
    ) {
        try {
            return ApiResponse.ok(
                    appointmentService.getAvailability(barbershopId, date, durationMin)
            );
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<AppointmentResponse> create(UUID barbershopId, AppointmentRequest req) {
        try {
            AppointmentResponse res = appointmentService.create(barbershopId, req);
            log.info("Cita agendada: {} {} {}", res.clientName(), res.appointmentDate(), res.startTime());
            return ApiResponse.ok("Cita agendada exitosamente", res);
        } catch (RuntimeException e) {
            log.warn("Error al agendar cita: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<List<AppointmentResponse>> findByDate(LocalDate date) {
        try {
            return ApiResponse.ok(appointmentService.findByDate(date));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<List<AppointmentResponse>> findPending() {
        try {
            return ApiResponse.ok(appointmentService.findPending());
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<List<AppointmentResponse>> findMyAgenda(LocalDate date) {
        try {
            return ApiResponse.ok(appointmentService.findMyAgenda(date));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<AppointmentResponse> assignBarber(UUID appointmentId, AssignBarberRequest req) {
        try {
            return ApiResponse.ok("Barbero asignado", appointmentService.assignBarber(appointmentId, req));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<AppointmentResponse> updateStatus(UUID appointmentId, String status) {
        try {
            return ApiResponse.ok("Estado actualizado", appointmentService.updateStatus(appointmentId, status));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<List<AppointmentResponse>> findByClient(UUID clientId) {
        try {
            return ApiResponse.ok(appointmentService.findByClient(clientId));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<BarberScheduleResponse> saveSchedule(UUID userId, BarberScheduleRequest req) {
        try {
            return ApiResponse.ok("Horario guardado", appointmentService.saveSchedule(userId, req));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<List<BarberScheduleResponse>> getSchedules(UUID userId) {
        try {
            return ApiResponse.ok(appointmentService.getSchedules(userId));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public ApiResponse<Void> addBlockedTime(UUID userId, BlockedTimeRequest req) {
        try {
            appointmentService.addBlockedTime(userId, req);
            return ApiResponse.ok("Bloqueo registrado", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}