package com.searise.sof.optimize.transformation;

import com.google.common.collect.ImmutableList;
import com.searise.sof.core.SofException;
import com.searise.sof.optimize.Group;
import com.searise.sof.optimize.GroupExpr;
import com.searise.sof.optimize.Operand;
import com.searise.sof.optimize.iter.Iterator;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ExprIter {
    public final Group group;
    public List<ExprIter> children = ImmutableList.of();
    private final Pattern pattern;
    private final Iterator<GroupExpr> iterator;
    private Optional<GroupExpr> value;

    private ExprIter(Group group, Pattern pattern) {
        this.group = group;
        this.pattern = pattern;
        this.iterator = group.iter().newReadOnlyIter();
        if (iterator.hasNext()) {
            this.value = Optional.of(iterator.next());
        }
    }

    private ExprIter(GroupExpr groupExpr, Pattern pattern) {
        this.group = groupExpr.group;
        this.pattern = pattern;
        this.iterator = new Iterator<GroupExpr>().add(groupExpr).newReadOnlyIter();
        this.value = Optional.of(iterator.next());
    }

    public GroupExpr getValue() {
        return value.orElseGet(() -> {
            throw new SofException("value is null");
        });
    }

    public boolean next() {
        if (!value.isPresent()) {
            return false;
        }

        for (int i = children.size() - 1; i >= 0; i--) {
            if (!children.get(i).next()) {
                continue;
            }

            for (int j = i + 1; j < children.size(); j++) {
                children.get(j).reset();
            }
            return true;
        }

        selfNext();
        return value.isPresent();
    }

    private void selfNext() {
        value = Optional.empty();
        while (iterator.hasNext()) {
            GroupExpr groupExpr = iterator.next();
            if (Operand.getOperand(groupExpr.exprNode).match(pattern.operand)) {
                continue;
            }

            if (pattern.children.isEmpty()) {
                value = Optional.of(groupExpr);
                break;
            }

            if (groupExpr.children.size() != pattern.children.size()) {
                continue;
            }

            Optional<List<ExprIter>> children = newChildren(groupExpr, pattern);
            if (children.isPresent()) {
                this.children = children.get();
                value = Optional.of(groupExpr);
                break;
            }
        }
    }

    public boolean reset() {
        this.iterator.reset();
        if (!iterator.hasNext()) {
            return false;
        }
        this.value = Optional.of(iterator.next());
        return doReset();
    }

    private boolean doReset() {
        while (true) {
            GroupExpr groupExpr = value.get();
            if (!Operand.getOperand(groupExpr.exprNode).match(pattern.operand)) {
                if (!iterator.hasNext()) {
                    return false;
                }
                value = Optional.of(iterator.next());
                continue;
            }

            if (pattern.children.isEmpty()) {
                return true;
            }

            if (groupExpr.children.size() != pattern.children.size()) {
                if (!iterator.hasNext()) {
                    return false;
                }
                value = Optional.of(iterator.next());
                continue;
            }

            Optional<List<ExprIter>> children = newChildren(groupExpr, pattern);
            if (children.isPresent()) {
                this.children = children.get();
                return true;
            }

            if (!iterator.hasNext()) {
                return false;
            }
            value = Optional.of(iterator.next());
        }
    }

    @Override
    public String toString() {
        return visitToString("");
    }

    private String visitToString(String preString) {
        String nextPreString = preString + "  ";
        return preString + getValue().exprNode.toString() + "\n" + children.stream().map(child -> child.visitToString(nextPreString)).collect(Collectors.joining());
    }

    public static Optional<ExprIter> newExprIter(GroupExpr groupExpr, Pattern pattern) {
        if (!Operand.getOperand(groupExpr.exprNode).match(pattern.operand)) {
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
        if (!iter.value.isPresent()) {
            return Optional.empty();
        }

        if (iter.doReset()) {
            return Optional.of(iter);
        }
        return Optional.empty();
    }
}
