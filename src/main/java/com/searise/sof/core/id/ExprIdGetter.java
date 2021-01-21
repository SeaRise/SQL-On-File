package com.searise.sof.core.id;

@FunctionalInterface
public interface ExprIdGetter {
    long apply(int index);
}
