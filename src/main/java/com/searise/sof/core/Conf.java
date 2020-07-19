package com.searise.sof.core;

import java.util.HashMap;
import java.util.Map;

public class Conf {
    public static final String FORCE_JOIN_TYPE = "sof_force_join_type";
    public static final String FORCE_JOIN_TYPE_DEFAULT_VALUE = "none";
    public static final String FORCE_JOIN_TYPE_LOOP_VALUE = "loop";
    public static final String FORCE_JOIN_TYPE_HASH_VALUE = "hash";

    public static final String AUTO_HASHJOIN_THRESHOLD = "sof_autoHashJoinThreshold";
    public static final String AUTO_HASHJOIN_THRESHOLD_DEFAULT_VALUE = "1000"; // 1000byte

    public Conf() {
        setConf(FORCE_JOIN_TYPE, FORCE_JOIN_TYPE_DEFAULT_VALUE);
        setConf(AUTO_HASHJOIN_THRESHOLD, AUTO_HASHJOIN_THRESHOLD_DEFAULT_VALUE);
    }

    private final Map<String, String> conf = new HashMap<>();

    public void setConf(String key, String value) {
        conf.put(key, value);
    }

    public String getConf(String key) {
        return conf.get(key);
    }

    public String getConf(String key, String defaultValue) {
        return conf.getOrDefault(key, defaultValue);
    }
}
