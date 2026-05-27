package com.bruno.barbershopapi.app.domain.repository;

import com.bruno.barbershopapi.app.domain.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SaleRepository extends JpaRepository<Sale, UUID> {

    List<Sale> findAllByBarbershopIdOrderByCreatedAtDesc(UUID barbershopId);

    // Ventas en un rango de fechas (para reportes)
    @Query("SELECT s FROM Sale s WHERE s.barbershop.id = :shopId " +
            "AND s.createdAt BETWEEN :from AND :to " +
            "AND s.status = 'completed' " +
            "ORDER BY s.createdAt DESC")
    List<Sale> findByBarbershopAndDateRange(
            @Param("shopId") UUID shopId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);

    List<Sale> findAllByBarbershopIdAndClientIdOrderByCreatedAtDesc(
            UUID barbershopId, UUID clientId);
}