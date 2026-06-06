package com.bruno.barbershopapi.app.domain.repository;

import com.bruno.barbershopapi.app.domain.entity.BarberProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BarberProfileRepository extends JpaRepository<BarberProfile, UUID> {

    // Todos los barberos de una barbería
    @Query("""
        SELECT bp FROM BarberProfile bp
        JOIN FETCH bp.user u
        WHERE bp.barbershop.id = :shopId
        ORDER BY u.fullName
    """)
    List<BarberProfile> findAllByBarbershopId(@Param("shopId") UUID shopId);

    // Uno por su user_id
    Optional<BarberProfile> findByUserId(UUID userId);

    // Verificar si ya existe perfil para ese usuario
    boolean existsByUserId(UUID userId);

    // Solo activos
    @Query("""
        SELECT bp FROM BarberProfile bp
        JOIN FETCH bp.user u
        WHERE bp.barbershop.id = :shopId
          AND bp.status = 'active'
        ORDER BY u.fullName
    """)
    List<BarberProfile> findActiveByBarbershopId(@Param("shopId") UUID shopId);
}