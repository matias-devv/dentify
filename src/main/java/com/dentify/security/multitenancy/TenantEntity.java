package com.dentify.security.multitenancy;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.TenantId;

@MappedSuperclass
@Getter
@Setter
public abstract class TenantEntity {

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;
}