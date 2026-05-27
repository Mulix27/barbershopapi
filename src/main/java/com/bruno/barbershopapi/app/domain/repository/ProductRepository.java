package com.bruno.barbershopapi.app.domain.repository;

import com.bruno.barbershopapi.app.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findAllByBarbershopIdAndIsActiveTrueOrderByNameAsc(UUID barbershopId);

    // Productos con stock bajo (para alertas)
    List<Product> findAllByBarbershopIdAndStockLessThanEqualAndIsActiveTrue(
            UUID barbershopId, Integer stockMin);
}