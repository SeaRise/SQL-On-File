package com.searise.sof.analyse;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

public interface AnalysisHelper<T extends AnalysisHelper> {
    default boolean resolved() {
        return false;
    }

    @SuppressWarnings("unchecked")
    default T transformDown(Applicable applicable) {
        T afterApplyPlan = (T) applicable.apply(this);

        ImmutableList.Builder<T> afterApplyChildrenBuilder = ImmutableList.builder();
        List<T> children = afterApplyPlan.children();
        for (T child : children) {
            afterApplyChildrenBuilder.add((T) child.transformDown(applicable));
        }
        List<T> afterApplyChildren = afterApplyChildrenBuilder.build();

        // only when children change should copyWithNewChildren
        if (!isEqualTo(children, afterApplyChildren)) {
            return (T) afterApplyPlan.copyWithNewChildren(afterApplyChildren);
        }
        return afterApplyPlan;
    }

    @SuppressWarnings("unchecked")
    default T transformUp(Applicable applicable) {
        ImmutableList.Builder<T> afterApplyChildrenBuilder = ImmutableList.builder();
        List<T> children = this.children();
        for (T child : children) {
            afterApplyChildrenBuilder.add((T) child.transformUp(applicable));
        }
        List<T> afterApplyChildren = afterApplyChildrenBuilder.build();

        T beforeApply = (T) this;
        // only when children change should copyWithNewChildren
        if (!isEqualTo(children, afterApplyChildren)) {
            beforeApply = (T) beforeApply.copyWithNewChildren(afterApplyChildren);
        }
        return (T) applicable.apply(beforeApply);
    }

    static <T extends AnalysisHelper> boolean isEqualTo(List<T> before, List<T> after) {
        if (Objects.isNull(before) || Objects.isNull(after) || before.size() != after.size()) {
            return false;
        }

        for (int i = 0; i < after.size(); i++) {
            if (after.get(i) != before.get(i)) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    default T copyWithNewChildren(List<T> children) {
        Preconditions.checkArgument(Objects.nonNull(children) && children.isEmpty());
        return (T) this;
    }

    default List<T> children() {
        return ImmutableList.of();
    }
}
