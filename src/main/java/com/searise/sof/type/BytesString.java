package com.searise.sof.type;

import java.nio.charset.StandardCharsets;

public class BytesString {
    private final byte[] bytes;

    private BytesString(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int length() {
        return bytes.length;
    }

    @Override
    public String toString() {
        return new String(bytes);
    }

    public static BytesString fromBytes(byte[] bytes) {
        return new BytesString(bytes);
    }

    public static BytesString fromString(String str) {
        return fromBytes(str.getBytes(StandardCharsets.UTF_8));
    }
}
