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
@Table(name = "users")
public class User extends BaseEntity {

    public static final String ROLE_OWNER     = "owner";
    public static final String ROLE_BARBER    = "barber";
    public static final String ROLE_CASHIER   = "cashier";
    public static final String ROLE_SECRETARY = "secretary";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbershop_id", nullable = false)
    private Barbershop barbershop;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "last_login")
    private OffsetDateTime lastLogin;

    public boolean isOwner()     { return ROLE_OWNER.equals(this.role); }
    public boolean isBarber()    { return ROLE_BARBER.equals(this.role); }
    public boolean isCashier()   { return ROLE_CASHIER.equals(this.role); }
    public boolean isSecretary() { return ROLE_SECRETARY.equals(this.role); }

    public boolean canManageAppointments() {
        return isOwner() || isSecretary();
    }

    public boolean canViewAllAppointments() {
        return isOwner() || isSecretary() || isCashier();
    }

}