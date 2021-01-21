package com.searise.sof.core.conf;

import java.util.Optional;

public class ConfVar<T> {
    private final String name;
    private final T defaultValue;
    private final ConfValueGet<T> confValueGet;

    private ConfVar(String name, T defaultValue, ConfValueGet<T> confValueGet) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.confValueGet = confValueGet;
    }

    public String getName() {
        return name;
    }

    public T getValue(Optional<String> optional) {
        return optional.map(confValueGet::getValue).orElse(defaultValue);
    }

    private static ConfValueGet<String> stringGet = str -> str;
    public static ConfVar<String> buildConf(String name, String defaultValue) {
        return new ConfVar<>(name, defaultValue, stringGet);
    }

    private static ConfValueGet<Integer> intGet = Integer::valueOf;
    public static ConfVar<Integer> buildConf(String name, Integer defaultValue) {
        return new ConfVar<>(name, defaultValue, intGet);
    }

    private static ConfValueGet<Long> longGet = Long::valueOf;
    public static ConfVar<Long> buildConf(String name, Long defaultValue) {
        return new ConfVar<>(name, defaultValue, longGet);
    }

    private static ConfValueGet<Double> doubleGet = Double::valueOf;
    public static ConfVar<Double> buildConf(String name, Double defaultValue) {
        return new ConfVar<Double>(name, defaultValue, doubleGet);
    }

    private static ConfValueGet<Boolean> boolGet = Boolean::valueOf;
    public static ConfVar<Boolean> buildConf(String name, Boolean defaultValue) {
        return new ConfVar<>(name, defaultValue, boolGet);
    }
}