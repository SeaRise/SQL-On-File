package com.searise.sof.optimize.transformation;

import com.google.common.collect.ImmutableList;
import com.searise.sof.core.SofException;
import com.searise.sof.optimize.Group;
import com.searise.sof.optimize.GroupExpr;
import com.searise.sof.optimize.Iterator;
import com.searise.sof.optimize.Operand;
import com.searise.sof.plan.logic.LogicalPlan;

import java.util.List;
import java.util.Optional;

public class ExprIter {
    public final Group group;
    public final List<ExprIter> children;
    public final Pattern pattern;
    public Iterator<GroupExpr> iterator;
    public Optional<GroupExpr> cur = Optional.empty();

    private ExprIter(Group group, List<ExprIter> children, Pattern pattern) {
        this.group = group;
        this.children = children;
        this.pattern = pattern;
        this.iterator = group.iter();
    }

    public boolean hasValue() {
        return cur.isPresent();
    }
    public GroupExpr getValue() {
        return cur.orElseGet(() -> {
            throw new SofException("has not value now!");
        });
    }

    public boolean next() {
        if (!cur.isPresent()) {
            if (!iterator.hasNext()) {
                return false;
            }
            cur = Optional.of(iterator.next());
            for (ExprIter child : children) {
                if (!child.next()) {
                    return false;
                }
            }
            return true;
        }

        for (ExprIter child : children) {
            if (child.next()) {
                return true;
            }
        }

        if (iterator.hasNext()) {
            cur = Optional.of(iterator.next());
            for (ExprIter child : children) {
                child.reset();
                if (!child.next()) {
                    return false;
                }
            }
        }
        return false;
    }

    public void reset() {
        iterator.reset();
    }

    public static Optional<ExprIter> newExprIter(GroupExpr groupExpr, Pattern pattern) {
        if (Operand.getOperand(groupExpr.exprNode) != pattern.operand) {
            return Optional.empty();
        }

        if (pattern.children.isEmpty()) {
            Iterator<LogicalPlan> iterator = new Iterator<>();
            iterator.add(groupExpr.exprNode);
            return Optional.of(new ExprIter(groupExpr.group, ImmutableList.of(), pattern, iterator));
        }

        if (groupExpr.children.size() != pattern.children.size()) {
            return Optional.empty();
        }

        ImmutableList.Builder<ExprIter> childBuilder = ImmutableList.builder();
        for (int i = 0; i < groupExpr.children.size(); i++) {
            Optional<ExprIter> child = newExprIter(groupExpr.children.get(i), pattern.children.get(i));
            if (!child.isPresent()) {
                return Optional.empty();
            }
            childBuilder.add(child.get());
        }
        Iterator<LogicalPlan> iterator = new Iterator<>();
        iterator.add(groupExpr.exprNode);
        return Optional.of(new ExprIter(groupExpr.group, childBuilder.build(), pattern, iterator));
    }

    private static Optional<ExprIter> newExprIter(Group group, Pattern pattern) {
        Iterator<LogicalPlan> iterator = new Iterator<>();
        Iterator<GroupExpr> groupExprIterator = group.iter();
        while (iterator.hasNext()) {
            GroupExpr groupExpr = iterator.next();
            newExprIter(groupExpr, pattern);
        }
//
//        if (Operand.getOperand(groupExpr.exprNode) != pattern.operand ||
//                groupExpr.children.size() != pattern.children.size()) {
//            return Optional.empty();
//        }
//
//        ImmutableList.Builder<ExprIter> itersBuild = ImmutableList.builder();
//        for (int i = 0; i < groupExpr.children.size(); i++) {
//            List<ExprIter> children = newExprIter(groupExpr.children.get(i), pattern.children.get(i));
//            if (children.isEmpty()) {
//                return ImmutableList.of();
//            }
//        }
//        return Optional.of(new ExprIter(group, childrenBuilder.build(), pattern));
        return Optional.empty();
    }
}
