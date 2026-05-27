package com.bruno.barbershopapi.app.domain.repository;

import com.bruno.barbershopapi.app.domain.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentReportRepository extends JpaRepository<Appointment, UUID> {

    // Métricas generales de citas en un período
    @Query(value = """
        SELECT
            COUNT(*)                                                    AS total,
            COUNT(*) FILTER (WHERE a.status = 'completed')             AS completed,
            COUNT(*) FILTER (WHERE a.status = 'cancelled')             AS cancelled,
            COUNT(*) FILTER (WHERE a.status = 'no_show')               AS no_show
        FROM appointments a
        WHERE a.barbershop_id = :shopId
        AND a.appointment_date BETWEEN :from AND :to
    """, nativeQuery = true)
    Object[] getAppointmentMetrics(
            @Param("shopId") UUID shopId,
            @Param("from")   LocalDate from,
            @Param("to")     LocalDate to);

    // Horas pico — en qué horas hay más citas
    @Query(value = """
        SELECT EXTRACT(HOUR FROM a.start_time)::INT AS hour,
               COUNT(*)                              AS total
        FROM appointments a
        WHERE a.barbershop_id = :shopId
        AND a.appointment_date BETWEEN :from AND :to
        AND a.status NOT IN ('cancelled', 'no_show')
        GROUP BY hour
        ORDER BY total DESC
    """, nativeQuery = true)
    List<Object[]> getPeakHours(
            @Param("shopId") UUID shopId,
            @Param("from")   LocalDate from,
            @Param("to")     LocalDate to);
}