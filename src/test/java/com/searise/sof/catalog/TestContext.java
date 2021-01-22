package com.searise.sof.catalog;

import com.searise.sof.core.SofContext;
import com.searise.sof.core.conf.SofConf;

public class TestContext {
    public static SofContext newTestContext() {
        return SofContext.getOrCreate(new SofConf().
                setConf("reserved_system_memory_bytes", "1024").
                setConf("system_memory", "1600"));
    }
}
