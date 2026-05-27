// ─── ClientHaircutRepository.java ────────────────────────────
package com.bruno.barbershopapi.app.domain.repository;

import com.bruno.barbershopapi.app.domain.entity.ClientHaircut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClientHaircutRepository extends JpaRepository<ClientHaircut, UUID> {

    List<ClientHaircut> findAllByClientIdOrderByIsPreferredDescCreatedAtDesc(UUID clientId);

    // Marcar todos como no-preferido antes de setear uno nuevo
    List<ClientHaircut> findAllByClientId(UUID clientId);
}