package com.searise.sof.core.conf;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Conf {
    private static final String FORCE_JOIN_TYPE_NONE_VALUE = "none";
    public static final String FORCE_JOIN_TYPE_LOOP_VALUE = "loop";
    public static final String FORCE_JOIN_TYPE_HASH_VALUE = "hash";
    public static final ConfVar<String> FORCE_JOIN_TYPE =
            ConfVar.buildConf("sof_force_join_type", FORCE_JOIN_TYPE_NONE_VALUE);

    public static final ConfVar<Long> AUTO_HASH_JOIN_THRESHOLD =
            ConfVar.buildConf("sof_auto_hash_join_threshold", 1000L);// 1000 bytes.

    public static final ConfVar<Boolean> CODEGEN_EXPRESSION =
            ConfVar.buildConf("sof_codegen_expression", false);

    public static final ConfVar<Boolean> CODEGEN_EXECUTOR =
            ConfVar.buildConf("sof_codegen_executor", false);

    public static final ConfVar<Long> WRITE_FLUSH_THRESHOLD =
            ConfVar.buildConf("sof_write_flush_threshold", 100L); // 1000 bytes.

    public static final ConfVar<Integer> MAX_PARALLELISM =
            ConfVar.buildConf("sof_max_parallelism", 10);

    public Conf() {
    }

    private final Map<String, String> conf = new HashMap<>();

    public void setConf(String name, String value) {
        conf.put(name, value);
    }

    public <T> T getConf(ConfVar<T> confVar) {
        return confVar.getValue(getConf(confVar.getName()));
    }

    public Optional<String> getConf(String name) {
        return Optional.ofNullable(conf.get(name));
    }
}
