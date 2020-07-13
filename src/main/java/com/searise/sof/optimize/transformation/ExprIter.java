package com.searise.sof.optimize.transformation;

import com.google.common.collect.ImmutableList;
import com.searise.sof.optimize.Group;
import com.searise.sof.optimize.GroupExpr;
import com.searise.sof.optimize.Operand;

import java.util.List;
import java.util.Optional;

public class ExprIter {
    public final Group group;
    public final List<ExprIter> children;
    public final Pattern pattern;

    private ExprIter(Group group, List<ExprIter> children, Pattern pattern) {
        this.group = group;
        this.children = children;
        this.pattern = pattern;
    }

    public static Optional<ExprIter> newExprIter(GroupExpr groupExpr, Pattern pattern) {
//        if (Operand.getOperand(groupExpr.exprNode) != pattern.operand ||
//                groupExpr.children.size() != pattern.children.size()) {
//            return Optional.empty();
//        }
//
//        ImmutableList.Builder<ExprIter> childrenBuilder = ImmutableList.builder();
//        for (int i = 0; i < groupExpr.children.size(); i++) {
//            Optional<ExprIter> childOptional = newExprIter(groupExpr.children.get(i), pattern.children.get(i));
//            if (!childOptional.isPresent()) {
//                return Optional.empty();
//            }
//            childrenBuilder.add(childOptional.get());
//        }
//        return Optional.of(new ExprIter(group, childrenBuilder.build(), pattern));
        return Optional.empty();
    }
}
