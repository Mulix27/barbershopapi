package com.bruno.barbershopapi.app.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "service_categories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ServiceCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbershop_id", nullable = false)
    private Barbershop barbershop;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String icon;                  // "pi-scissors", "pi-star", etc.

    // 'fixed' = un precio por categoría
    // 'variants' = precio por variante/estilo
    @Column(name = "pricing_mode", nullable = false, length = 20)
    private String pricingMode = "fixed";

    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;         // solo si pricingMode = 'fixed'

    @Column(name = "base_duration")
    private Integer baseDuration;         // minutos, solo si pricingMode = 'fixed'

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @OneToMany(
            mappedBy   = "category",
            cascade    = CascadeType.ALL,
            orphanRemoval = true,
            fetch      = FetchType.LAZY
    )
    @OrderBy("sortOrder ASC, createdAt ASC")
    @Builder.Default
    private List<ServiceVariant> variants = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // ── Helper para agregar variante ──────────────────────────
    public void addVariant(ServiceVariant variant) {
        variant.setCategory(this);
        this.variants.add(variant);
    }

    public void clearVariants() {
        this.variants.clear();
    }
}