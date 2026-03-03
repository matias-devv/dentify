package com.dentify.security.multitenancy;

public class TenantContext {

    private static final ThreadLocal<String> current = new ThreadLocal<>();

    public static void set(String tenantId) { current.set(tenantId); }

    public static String get() { return current.get(); }

    public static void clear() { current.remove(); }
}