package com.searise.sof.type;

public class Bytes {
    public static byte zero = 0;
    public static byte one = 1;

    public static boolean byteToBoolean(byte b) {
        return b != 0;
    }

    public static byte booleanToByte(boolean b) {
        return b ? one : zero;
    }
}
