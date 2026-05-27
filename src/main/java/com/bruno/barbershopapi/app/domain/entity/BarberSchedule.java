package com.bruno.barbershopapi.app.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "barber_schedules",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "day_of_week"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BarberSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "barbershop_id", nullable = false)
    private Barbershop barbershop;

    /**
     * Día de la semana ISO 8601: 1=Lunes … 7=Domingo
     */
    @Column(name = "day_of_week", nullable = false)
    private Short dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /**
     * Duración en minutos de cada slot de cita. Default: 30 min.
     * Ej: si trabaja 9am-6pm con slot 30min → 18 slots disponibles por día.
     */
    @Column(name = "slot_duration", nullable = false)
    private Short slotDuration = 30;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}