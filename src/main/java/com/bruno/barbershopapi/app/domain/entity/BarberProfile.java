package com.bruno.barbershopapi.app.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "barber_profiles")
public class BarberProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ── Relación con users ─────────────────────────────────────
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbershop_id", nullable = false)
    private Barbershop barbershop;

    // ── Perfil público ─────────────────────────────────────────
    @Column(length = 120)
    private String specialty;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "photo_url", columnDefinition = "TEXT")
    private String photoUrl;

    @Column(name = "photo_public_id", columnDefinition = "TEXT")
    private String photoPublicId;   // Cloudinary public_id

    // ── Estado laboral ─────────────────────────────────────────
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "active";   // active | on_break | inactive

    // ── Métricas ───────────────────────────────────────────────
    @Column(nullable = false, precision = 3, scale = 1)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "total_cuts", nullable = false)
    @Builder.Default
    private Integer totalCuts = 0;

    // ── Auditoría ──────────────────────────────────────────────
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}