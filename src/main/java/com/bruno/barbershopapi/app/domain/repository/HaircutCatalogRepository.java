package com.bruno.barbershopapi.app.domain.repository;

import com.bruno.barbershopapi.app.domain.entity.HaircutCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HaircutCatalogRepository extends JpaRepository<HaircutCatalog, UUID> {

    List<HaircutCatalog> findAllByBarbershopIdAndIsActiveTrueOrderByNameAsc(UUID barbershopId);

    List<HaircutCatalog> findAllByBarbershopIdOrderByNameAsc(UUID barbershopId);
}