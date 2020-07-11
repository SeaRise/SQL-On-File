package com.searise.sof.common;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.stream.Stream;

public class Utils {
    public static <T>  List<T> toImmutableList(Stream<T> stream) {
        ImmutableList.Builder<T> builder = ImmutableList.builder();
        stream.forEach(builder::add);
        return builder.build();
    }
}
