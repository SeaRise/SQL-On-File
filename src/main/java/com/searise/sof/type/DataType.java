package com.searise.sof.type;

import com.searise.sof.core.SofException;
import org.apache.commons.lang3.StringUtils;

public enum DataType {
    BooleanType((byte) 0, "bool", "Boolean", 4, 1),
    StringType((byte) 1, "string", "String", 20, 20),
    IntegerType((byte) 2, "int", "Integer", 4, 4),
    DoubleType((byte) 3, "double", "Double", 8, 8);

    private final byte flagAndPriority;
    public final String name;
    public final String javaType;
    public final int defaultJVMSize;
    public final int defaultByteSize;

    DataType(byte flagAndPriority, String name, String javaType,  int defaultJVMSize, int defaultByteSize) {
        this.flagAndPriority = flagAndPriority;
        this.name = name;
        this.javaType = javaType;
        this.defaultJVMSize = defaultJVMSize;
        this.defaultByteSize = defaultByteSize;
    }

    public final byte getPriority() {
        return flagAndPriority;
    }

    public final byte getFlag() {
        return flagAndPriority;
    }

    public static DataType getType(String name) {
        for (DataType dataType : values()) {
            if (StringUtils.equals(dataType.name, name)) {
                return dataType;
            }
        }
        throw new SofException("no such data type: " + name);
    }

    public static DataType getType(byte flag) {
        for (DataType dataType : values()) {
            if (dataType.getFlag() == flag) {
                return dataType;
            }
        }
        throw new SofException("no such data type: " + flag);
    }

    public static DataType getType(Class<?> clazz) {
        for (DataType dataType : values()) {
            if (StringUtils.equals(dataType.javaType, clazz.getSimpleName())) {
                return dataType;
            }
        }
        throw new SofException("no such data type: " + clazz);
    }
}