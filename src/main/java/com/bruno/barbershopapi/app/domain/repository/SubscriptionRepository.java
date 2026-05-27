package com.bruno.barbershopapi.app.domain.repository;

import com.bruno.barbershopapi.app.domain.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByBarbershopIdAndStatus(UUID barbershopId, String status);
}