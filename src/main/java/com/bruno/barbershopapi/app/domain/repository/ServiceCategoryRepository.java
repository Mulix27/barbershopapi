package com.bruno.barbershopapi.app.domain.repository;

import com.bruno.barbershopapi.app.domain.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, UUID> {

    @Query("""
        SELECT DISTINCT c FROM ServiceCategory c
        LEFT JOIN FETCH c.variants v
        WHERE c.barbershop.id = :shopId
        AND c.isActive = true
        ORDER BY c.sortOrder ASC, c.createdAt ASC
    """)
    List<ServiceCategory> findAllActiveByShop(@Param("shopId") UUID shopId);

    @Query("""
        SELECT DISTINCT c FROM ServiceCategory c
        LEFT JOIN FETCH c.variants
        WHERE c.barbershop.id = :shopId
        ORDER BY c.sortOrder ASC, c.createdAt ASC
    """)
    List<ServiceCategory> findAllByShop(@Param("shopId") UUID shopId);

    Optional<ServiceCategory> findByIdAndBarbershopId(UUID id, UUID shopId);
}