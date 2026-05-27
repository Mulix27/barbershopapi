package com.bruno.barbershopapi.app.domain.repository;

import com.bruno.barbershopapi.app.domain.entity.ServiceVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceVariantRepository extends JpaRepository<ServiceVariant, UUID> {

    List<ServiceVariant> findByCategoryIdAndIsActiveTrue(UUID categoryId);

    @Query("""
        SELECT v FROM ServiceVariant v
        WHERE v.category.barbershop.id = :shopId
        AND v.isActive = true
        ORDER BY v.category.sortOrder ASC, v.sortOrder ASC
    """)
    List<ServiceVariant> findAllActiveByShop(@Param("shopId") UUID shopId);
}