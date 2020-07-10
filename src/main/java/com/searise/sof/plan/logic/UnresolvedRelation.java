package com.searise.sof.plan.logic;

import java.util.Optional;

public class UnresolvedRelation implements LogicalPlan {
    public final String tableName; // real name
    public final Optional<String> referenceName; // alias

    public UnresolvedRelation(String tableName) {
        this.referenceName = Optional.empty();
        this.tableName = tableName;
    }

    public UnresolvedRelation(String tableName, Optional<String> referenceName) {
        this.tableName = tableName;
        this.referenceName = referenceName;
    }

    @Override
    public String toString() {
        String aliasToString = referenceName.map(s -> ", " + s).orElse("");
        return String.format("UnresolvedRelation [%s%s]", tableName, aliasToString);
    }
}
