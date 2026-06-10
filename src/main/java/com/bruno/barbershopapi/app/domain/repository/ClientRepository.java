// ─── ClientRepository.java ───────────────────────────────────
package com.bruno.barbershopapi.app.domain.repository;

import com.bruno.barbershopapi.app.domain.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    // Búsqueda principal en el punto de venta
    Optional<Client> findByBarbershopIdAndPhone(UUID barbershopId, String phone);

    // Búsqueda por nombre (contiene, sin importar mayúsculas)
    List<Client> findByBarbershopIdAndFullNameContainingIgnoreCaseAndIsActiveTrue(
            UUID barbershopId, String fullName);

    // Listar todos los activos
    List<Client> findAllByBarbershopIdAndIsActiveTrueOrderByFullNameAsc(UUID barbershopId);

    boolean existsByBarbershopIdAndPhoneAndIsActiveTrue(UUID barbershopId, String phone);

    @Query(value = """
    SELECT *
    FROM clients c
    WHERE c.barbershop_id = :barbershopId
    AND RIGHT(REGEXP_REPLACE(COALESCE(c.phone, ''), '[^0-9]', '', 'g'), 10) = :phoneLast10
    ORDER BY c.created_at DESC
    LIMIT 1
    """, nativeQuery = true)
    Optional<Client> findByBarbershopIdAndPhoneLast10(
            @Param("barbershopId") UUID barbershopId,
            @Param("phoneLast10") String phoneLast10
    );
}