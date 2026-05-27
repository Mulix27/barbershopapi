package com.bruno.barbershopapi.app.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "plans")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "price_monthly", nullable = false, precision = 8, scale = 2)
    private BigDecimal priceMonthly;

    @Column(name = "price_yearly", precision = 8, scale = 2)
    private BigDecimal priceYearly;

    @Column(name = "max_users", nullable = false)
    private Integer maxUsers;

    @Column(name = "has_reports", nullable = false)
    private Boolean hasReports;

    @Column(name = "has_custom_page", nullable = false)
    private Boolean hasCustomPage;

    @Column(name = "has_inventory", nullable = false)
    private Boolean hasInventory;

    @Column(name = "max_clients")
    private Integer maxClients;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}