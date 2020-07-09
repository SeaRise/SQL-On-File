package com.searise.sof.plan;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;

public class Relation implements LogicalPlan {
    public final String tableName; // real name
    public final Optional<String> referenceName; // alias

    public Relation(String tableName) {
        this.referenceName = Optional.empty();
        this.tableName = tableName;
    }

    public Relation(String tableName, String referenceName) {
        this.tableName = tableName;
        this.referenceName = Optional.of(referenceName);
    }

    @Override
    public List<LogicalPlan> children() {
        return ImmutableList.of();
    }

    @Override
    public String toString() {
        String aliasToString = referenceName.map(s -> ", " + s).orElse("");
        return String.format("Relation [%s%s]", tableName, aliasToString);
    }
}
