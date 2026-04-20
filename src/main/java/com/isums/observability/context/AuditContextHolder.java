package com.isums.observability.context;

public final class AuditContextHolder {

    private static final ThreadLocal<AuditContext> HOLDER = new ThreadLocal<>();

    private AuditContextHolder() {
    }

    public static AuditContext get() {
        return HOLDER.get();
    }

    public static void set(AuditContext context) {
        HOLDER.set(context);
    }

    public static void clear() {
        HOLDER.remove();
    }
}
