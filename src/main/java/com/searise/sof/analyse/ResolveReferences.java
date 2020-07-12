package com.searise.sof.analyse;

import com.searise.sof.catalog.Catalog;
import com.searise.sof.plan.logic.LogicalPlan;
import com.searise.sof.plan.logic.Relation;
import com.searise.sof.plan.logic.UnresolvedRelation;

public class ResolveReferences implements Rule {
    private final Catalog catalog;

    public ResolveReferences(Catalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public LogicalPlan apply(LogicalPlan plan) {
        return plan.transformUp(p -> {
            if (p.getClass() == UnresolvedRelation.class) {
                UnresolvedRelation unresolvedRelation = (UnresolvedRelation) p;
                return new Relation(
                        catalog.getTable(unresolvedRelation.tableName),
                        unresolvedRelation.tableName,
                        unresolvedRelation.referenceName);
            }
            return p;
        });
    }
}