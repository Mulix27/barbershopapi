package com.bruno.barbershopapi.app.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "haircut_photos")
public class HaircutPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_haircut_id", nullable = false)
    private ClientHaircut clientHaircut;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "public_id")
    private String publicId;

    @Column(name = "storage_provider", length = 20)
    private String storageProvider;

    @Column(name = "taken_at", nullable = false)
    private OffsetDateTime takenAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id")
    private User uploadedByUser;
}