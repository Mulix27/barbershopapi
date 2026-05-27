package com.bruno.barbershopapi.app.domain.repository;

import com.bruno.barbershopapi.app.domain.entity.BarberBlockedTime;
import com.bruno.barbershopapi.app.domain.entity.BarberSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BarberScheduleRepository extends JpaRepository<BarberSchedule, UUID> {

    // Horario de un barbero en un día de la semana
    Optional<BarberSchedule> findByUserIdAndDayOfWeekAndIsActiveTrue(
            UUID userId, Short dayOfWeek);

    Optional<BarberSchedule> findByUserIdAndDayOfWeek(UUID userId, Short dayOfWeek);

    // Todos los horarios activos de un barbero
    List<BarberSchedule> findAllByUserIdAndIsActiveTrueOrderByDayOfWeekAsc(UUID userId);

    // Todos los barberos con horario en un día específico (para multi-barbero)
    List<BarberSchedule> findAllByBarbershopIdAndDayOfWeekAndIsActiveTrue(
            UUID barbershopId, Short dayOfWeek);

    List<BarberSchedule> findAllByUserIdOrderByDayOfWeekAsc(UUID userId);
}