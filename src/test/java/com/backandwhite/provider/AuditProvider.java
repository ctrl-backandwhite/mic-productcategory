package com.backandwhite.provider;

import java.time.Instant;

public final class AuditProvider {

    public static final Instant CREATED_AT = Instant.parse("2026-02-16T10:15:30Z");
    public static final Instant UPDATED_AT = Instant.parse("2026-02-16T11:05:00Z");

    private AuditProvider() {
    }
}
