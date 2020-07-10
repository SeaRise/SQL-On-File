package com.searise.sof.plan;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.stream.Collectors;

public interface QueryPlan<T extends QueryPlan> {
    default List<T> children() {
        return ImmutableList.of();
    }

    default String visitToString() {
        return visitToString("");
    }

    default String visitToString(String preString) {
        String nextPreString = preString + "  ";
        return preString + toString() + "\n" + children().stream().map(child -> child.visitToString(nextPreString)).collect(Collectors.joining());
    }
}
