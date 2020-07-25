package com.searise.sof.core;

import java.util.HashMap;
import java.util.Map;

public class Conf {
    public static final String FORCE_JOIN_TYPE = "sof_force_join_type";
    public static final String FORCE_JOIN_TYPE_DEFAULT_VALUE = "none";
    public static final String FORCE_JOIN_TYPE_LOOP_VALUE = "loop";
    public static final String FORCE_JOIN_TYPE_HASH_VALUE = "hash";

    public static final String AUTO_HASH_JOIN_THRESHOLD = "sof_auto_hash_join_threshold";
    public static final String AUTO_HASH_JOIN_THRESHOLD_DEFAULT_VALUE = "1000"; // 1000 bytes.

    public static final String CODEGEN_EXPRESSION = "sof_codegen_expression";
    public static final String CODEGEN_EXPRESSION_DEFAULT_VALUE = "true";

    public static final String WRITE_FLUSH_THRESHOLD = "sof_write_flush_threshold";
    public static final String WRITE_FLUSH_THRESHOLD_DEFAULT_VALUE = "100"; // 1000 bytes.

    public Conf() {
        setConf(FORCE_JOIN_TYPE, FORCE_JOIN_TYPE_DEFAULT_VALUE);
        setConf(AUTO_HASH_JOIN_THRESHOLD, AUTO_HASH_JOIN_THRESHOLD_DEFAULT_VALUE);
        setConf(CODEGEN_EXPRESSION, CODEGEN_EXPRESSION_DEFAULT_VALUE);
        setConf(WRITE_FLUSH_THRESHOLD, WRITE_FLUSH_THRESHOLD_DEFAULT_VALUE);
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

    public int getIntConf(String key) {
        return Integer.valueOf(getConf(key));
    }

    public boolean getBoolConf(String key) {
        return Boolean.valueOf(getConf(key));
    }
}
