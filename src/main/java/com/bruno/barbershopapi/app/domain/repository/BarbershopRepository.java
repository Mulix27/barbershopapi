package com.bruno.barbershopapi.app.domain.repository;

import com.bruno.barbershopapi.app.domain.entity.Barbershop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BarbershopRepository extends JpaRepository<Barbershop, UUID> {

    Optional<Barbershop> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySubdomain(String subdomain);
}