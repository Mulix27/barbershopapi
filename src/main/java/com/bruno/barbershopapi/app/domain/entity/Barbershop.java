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
@Table(name = "barbershops")
public class Barbershop extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 80)
    private String slug;

    @Column(length = 20)
    private String phone;

    @Column(length = 120)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 80)
    private String city;

    @Column(name = "logo_url", columnDefinition = "TEXT")
    private String logoUrl;

    @Column(name = "primary_color", length = 7)
    private String primaryColor;

    @Column(unique = true, length = 80)
    private String subdomain;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "single_barber", nullable = false)
    private Boolean singleBarber = false;

}