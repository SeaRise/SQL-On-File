package com.searise.sof.type;

public class TypeUtils {
    public static int getJVMSize(Object obj) {
        if (obj instanceof Boolean) {
            return 4;
        } else if (obj instanceof String) {
            return obj.toString().length();
        } else if (obj instanceof Integer) {
            return 4;
        } else if (obj instanceof Double) {
            return 8;
        } else {
            throw new IllegalArgumentException("unknown type: " + obj.getClass().getSimpleName());
        }
    }

    public static int getBytesSize(Object obj) {
        if (obj instanceof Boolean) {
            return 1;
        } else if (obj instanceof String) {
            return 4 + obj.toString().length();
        } else if (obj instanceof Integer) {
            return 4;
        } else if (obj instanceof Double) {
            return 8;
        } else {
            throw new IllegalArgumentException("unknown type: " + obj.getClass().getSimpleName());
        }
    }
}
