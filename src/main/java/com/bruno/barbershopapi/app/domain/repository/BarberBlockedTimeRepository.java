package com.bruno.barbershopapi.app.domain.repository;

import com.bruno.barbershopapi.app.domain.entity.BarberBlockedTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// ─── BarberBlockedTimeRepository.java ────────────────────────────────────────
@Repository
public interface BarberBlockedTimeRepository extends JpaRepository<BarberBlockedTime, UUID> {

    // Bloqueos de un barbero en una fecha
    @Query("""
        SELECT b FROM BarberBlockedTime b
        WHERE b.user.id = :userId
        AND b.blockedDate = :date
    """)
    List<BarberBlockedTime> findByUserAndDate(
            @Param("userId") UUID userId,
            @Param("date")   LocalDate date);

    // Bloqueos de todos los barberos de una barbería en una fecha
    @Query("""
        SELECT b FROM BarberBlockedTime b
        WHERE b.barbershop.id = :shopId
        AND b.blockedDate = :date
    """)
    List<BarberBlockedTime> findByBarbershopAndDate(
            @Param("shopId") UUID shopId,
            @Param("date")   LocalDate date);
}