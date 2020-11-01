package com.searise.sof.analyse;

public interface Rule<T extends AnalysisHelper> {
    T apply(T plan);

    default String ruleName() {
        return this.getClass().getSimpleName();
    }
}
