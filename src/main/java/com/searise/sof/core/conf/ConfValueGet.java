package com.searise.sof.core.conf;

@FunctionalInterface
public interface ConfValueGet<T> {
    T getValue(String str);
}