package com.bruno.barbershopapi.app.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import java.math.BigDecimal;

@Entity
@Table(name = "appointments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Appointment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "barbershop_id", nullable = false)
    private Barbershop barbershop;

    // Cliente registrado — puede ser null si es cliente de paso
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    // Siempre se guarda aunque no esté registrado en el sistema
    @Column(name = "client_name", nullable = false, length = 120)
    private String clientName;

    @Column(name = "client_phone", nullable = false, length = 20)
    private String clientPhone;

    @Column(name = "client_notes", columnDefinition = "TEXT")
    private String clientNotes;

    // NULL hasta que el encargado asigne (o auto-asignado en single_barber)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    // Servicio del catálogo — opcional
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "haircut_catalog_id")
    private HaircutCatalog haircutCatalog;

    @Column(name = "service_category_id")
    private UUID serviceCategoryId;

    @Column(name = "service_variant_id")
    private UUID serviceVariantId;

    @Column(name = "service_name", length = 150)
    private String serviceName;

    @Column(name = "service_price", precision = 10, scale = 2)
    private BigDecimal servicePrice;

    @Column(name = "service_duration_min")
    private Integer serviceDurationMin;

    @Column(name = "service_notes", columnDefinition = "TEXT")
    private String serviceNotes;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status = AppointmentStatus.pending;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentSource source = AppointmentSource.web;

    // Se llena cuando la cita se convierte en venta
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id")
    private Sale sale;

    @Column(name = "reminder_sent", nullable = false)
    private Boolean reminderSent = false;

    public enum AppointmentStatus {
        pending,        // sin barbero — esperando asignación
        confirmed,      // barbero asignado (o auto-asignado en single_barber)
        in_progress,    // cliente llegó, se está atendiendo
        completed,      // terminado — se puede convertir en venta
        cancelled,
        no_show
    }

    public enum AppointmentSource {
        web,            // desde la página pública de la barbería
        whatsapp,       // llegó por WhatsApp
        dashboard       // agendada manualmente por el encargado
    }
}