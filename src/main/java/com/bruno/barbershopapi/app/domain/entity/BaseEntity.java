package com.bruno.barbershopapi.app.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    @Column(name = "created_at", nullable = false)
    protected OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    protected OffsetDateTime updatedAt;

    @PrePersist
    protected void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();

        if (this.createdAt == null) {
            this.createdAt = now;
        }

        this.updatedAt = now;
    }

    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}