package com.bruno.barbershopapi.app.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "barber_blocked_times")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BarberBlockedTime {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "barbershop_id", nullable = false)
    private Barbershop barbershop;

    @Column(name = "blocked_date", nullable = false)
    private LocalDate blockedDate;

    /**
     * NULL en ambos campos = bloqueo de todo el día (vacaciones, día libre).
     * Con valores = bloqueo de un rango específico (ej: comida 2pm-3pm).
     */
    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(length = 120)
    private String reason;               // "Vacaciones", "Comida", "Cita médica"

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}