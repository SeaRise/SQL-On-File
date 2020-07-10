package com.searise.sof.analyse;

@FunctionalInterface
public interface Applicable<T extends AnalysisHelper> {
    T apply(T t);
}
