package com.searise.sof.schedule.dag;

import java.util.Objects;

public class PlanExecResult {
    private final Throwable failCause;

    private PlanExecResult(Throwable failCause) {
        this.failCause = failCause;
    }

    public void execResult() throws Throwable {
        if (Objects.nonNull(failCause)) {
            throw failCause;
        }
    }

    public static PlanExecResult success() {
        return new PlanExecResult(null);
    }

    public static PlanExecResult fail(Throwable e) {
        return new PlanExecResult(e);
    }
}
