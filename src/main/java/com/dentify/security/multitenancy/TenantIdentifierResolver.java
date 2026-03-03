package com.dentify.security.multitenancy;

import lombok.RequiredArgsConstructor;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String>, HibernatePropertiesCustomizer {

    @Override
    public String resolveCurrentTenantIdentifier() {

        String tenantId = TenantContext.get();
        // Hibernate throws exception if it's null
        return (tenantId != null) ? tenantId : "UNKNOWN";
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }

    @Override
    public void customize( Map<String, Object> hibernateProperties) {
        hibernateProperties.put( AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this );
    }
}