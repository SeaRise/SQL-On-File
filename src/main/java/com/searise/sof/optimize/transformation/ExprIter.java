package com.searise.sof.optimize.transformation;

import com.google.common.collect.ImmutableList;
import com.searise.sof.optimize.Group;
import com.searise.sof.optimize.GroupExpr;
import com.searise.sof.optimize.Operand;
import com.searise.sof.optimize.iter.Iterator;

import java.util.List;
import java.util.Optional;

public class ExprIter {
    public final Group group;
    private List<ExprIter> children = ImmutableList.of();
    private final Pattern pattern;
    private final Iterator<GroupExpr> iterator;
    private Optional<GroupExpr> cur;
    private GroupExpr value;

    private ExprIter(Group group, Pattern pattern) {
        this.group = group;
        this.pattern = pattern;
        this.iterator = group.iter().newReadOnlyIter();
        if (iterator.hasNext()) {
            this.cur = Optional.of(iterator.next());
        }
    }

    private ExprIter(GroupExpr groupExpr, Pattern pattern) {
        this.group = groupExpr.group;
        this.pattern = pattern;
        this.iterator = new Iterator<GroupExpr>().add(groupExpr).newReadOnlyIter();
        this.cur = Optional.of(iterator.next());
    }

    public GroupExpr getValue() {
        return value;
    }

    public boolean next() {
        if (!cur.isPresent()) {
            return false;
        }
        value = cur.get();

        for (ExprIter child : children) {
            if (child.next()) {
                return true;
            }
        }

        selfNext();

        return true;
    }

    private void selfNext() {
        cur = Optional.empty();
        while (iterator.hasNext()) {
            GroupExpr groupExpr = iterator.next();
            if (Operand.getOperand(groupExpr.exprNode) != pattern.operand) {
                continue;
            }

            if (pattern.children.isEmpty()) {
                cur = Optional.of(groupExpr);
                break;
            }

            if (groupExpr.children.size() != pattern.children.size()) {
                continue;
            }

            Optional<List<ExprIter>> children = newChildren(groupExpr, pattern);
            if (children.isPresent()) {
                this.children = children.get();
                cur = Optional.of(groupExpr);
                break;
            }
        }
    }

    public static Optional<ExprIter> newExprIter(GroupExpr groupExpr, Pattern pattern) {
        if (Operand.getOperand(groupExpr.exprNode) != pattern.operand) {
            return Optional.empty();
        }

        if (pattern.children.isEmpty()) {
            return Optional.of(new ExprIter(groupExpr, pattern));
        }

        if (groupExpr.children.size() != pattern.children.size()) {
            return Optional.empty();
        }

        Optional<List<ExprIter>> children = newChildren(groupExpr, pattern);
        if (!children.isPresent()) {
            return Optional.empty();
        }

        ExprIter iter = new ExprIter(groupExpr, pattern);
        iter.children = children.get();
        return Optional.of(iter);
    }

    private static Optional<List<ExprIter>> newChildren(GroupExpr groupExpr, Pattern pattern) {
        ImmutableList.Builder<ExprIter> childBuilder = ImmutableList.builder();
        for (int i = 0; i < groupExpr.children.size(); i++) {
            Optional<ExprIter> child = newExprIter(groupExpr.children.get(i), pattern.children.get(i));
            if (!child.isPresent()) {
                return Optional.empty();
            }
            childBuilder.add(child.get());
        }
        return Optional.of(childBuilder.build());
    }

    private static Optional<ExprIter> newExprIter(Group group, Pattern pattern) {
        ExprIter iter = new ExprIter(group, pattern);
        if (!iter.cur.isPresent()) {
            return Optional.empty();
        }

        while (true) {
            GroupExpr groupExpr = iter.cur.get();
            if (Operand.getOperand(groupExpr.exprNode) != pattern.operand) {
                continue;
            }

            if (pattern.children.isEmpty()) {
                return Optional.of(iter);
            }

            if (groupExpr.children.size() != pattern.children.size()) {
                continue;
            }

            Optional<List<ExprIter>> children = newChildren(groupExpr, pattern);
            if (children.isPresent()) {
                return Optional.of(iter);
            }

            if (!iter.iterator.hasNext()) {
                return Optional.empty();
            }

            iter.cur = Optional.of(iter.iterator.next());
        }
    }
}
