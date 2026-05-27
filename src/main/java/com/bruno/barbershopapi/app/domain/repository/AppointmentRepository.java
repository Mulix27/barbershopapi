package com.bruno.barbershopapi.app.domain.repository;

import com.bruno.barbershopapi.app.domain.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    // Todas las citas de una barbería en una fecha (vista del encargado)
    List<Appointment> findAllByBarbershopIdAndAppointmentDateOrderByStartTimeAsc(
            UUID barbershopId, LocalDate date);

    // Citas de un barbero específico en una fecha (su agenda del día)
    List<Appointment> findAllByAssignedToIdAndAppointmentDateOrderByStartTimeAsc(
            UUID userId, LocalDate date);

    // Citas pendientes de asignación
    List<Appointment> findAllByBarbershopIdAndStatusOrderByAppointmentDateAscStartTimeAsc(
            UUID barbershopId, String status);

    // Historial de citas de un cliente
    List<Appointment> findAllByBarbershopIdAndClientIdOrderByAppointmentDateDescStartTimeDesc(
            UUID barbershopId, UUID clientId);

    // Verificar conflicto de horario: ¿ya hay una cita en ese slot para ese barbero?
    @Query("""
        SELECT COUNT(a) > 0 FROM Appointment a
        WHERE a.assignedTo.id = :userId
        AND a.appointmentDate = :date
        AND a.status NOT IN ('cancelled', 'no_show')
        AND (
            (a.startTime < :endTime AND a.endTime > :startTime)
        )
    """)
    boolean existsConflict(
            @Param("userId")    UUID userId,
            @Param("date")      LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime")   LocalTime endTime);

    // Slots ocupados de una barbería en una fecha (para calcular disponibilidad)
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.barbershop.id = :shopId
        AND a.appointmentDate = :date
        AND a.status NOT IN ('cancelled', 'no_show')
        ORDER BY a.startTime ASC
    """)
    List<Appointment> findActiveByBarbershopAndDate(
            @Param("shopId") UUID shopId,
            @Param("date")   LocalDate date);
}