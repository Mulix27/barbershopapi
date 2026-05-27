// ─── ClientRepository.java ───────────────────────────────────
package com.bruno.barbershopapi.app.domain.repository;

import com.bruno.barbershopapi.app.domain.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
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

    boolean existsByBarbershopIdAndPhone(UUID barbershopId, String phone);
}