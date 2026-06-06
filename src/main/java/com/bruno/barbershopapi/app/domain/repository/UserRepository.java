package com.bruno.barbershopapi.app.domain.repository;

import com.bruno.barbershopapi.app.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByBarbershopIdAndEmail(UUID barbershopId, String email);

    // ✅ User.Role es el enum interno de la entidad User
    Optional<User> findByBarbershopIdAndRole(UUID barbershopId, String Role);

    Optional<User> findByEmailIgnoreCaseAndIsActiveTrue(String email);

    List<User> findAllByBarbershopIdAndRoleAndIsActiveTrue(UUID barbershopId, String role);

    List<User> findAllByBarbershopIdAndRoleInAndIsActiveTrue(UUID barbershopId, List<String> roles);

    boolean existsByBarbershopIdAndEmail(UUID barbershopId, String email);
}