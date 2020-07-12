package com.searise.sof.plan.logic;

import com.searise.sof.core.SofException;
import com.searise.sof.expression.attribute.Attribute;

import java.util.List;
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

    @Override
    public List<Attribute> schema() {
        throw new SofException("unresolved plan can not call schema");
    }
}
