package com.searise.sof.catalog;

import com.searise.sof.core.SofSession;

public class TestSession {
    public static SofSession newTestSession() {
        return SofSession.builder().
                config("reserved_system_memory_bytes", "1024").
                config("system_memory", "1600").
                build();
    }
}
