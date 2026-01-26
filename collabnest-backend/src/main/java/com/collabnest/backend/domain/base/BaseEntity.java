package com.collabnest.backend.domain.base;

import jakarta.persistence.*;
import java.time.Instant;

@MappedSuperclass
public abstract class BaseEntity {

    @Column(updatable = false)
    protected Instant createdAt = Instant.now();

    protected Instant updatedAt = Instant.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
