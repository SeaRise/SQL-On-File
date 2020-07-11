package com.searise.sof.expression.attribute;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.expression.Expression;

import java.util.List;
import java.util.Objects;

public class Alias implements Expression {
    public final AttributeBase attribute;
    public final Expression child;

    public Alias(AttributeBase attribute, Expression child) {
        this.attribute = attribute;
        this.child = child;
    }

    @Override
    public String toString() {
        return String.format("%s as %s", child, attribute);
    }

    @Override
    public Expression copyWithNewChildren(List<Expression> children) {
        Preconditions.checkArgument(Objects.nonNull(children) && children.size() == 1);
        return new Alias(attribute, children.get(0));
    }

    @Override
    public List<Expression> children() {
        return ImmutableList.of(child);
    }

    public boolean resolved() {
        return attribute.resolved() && child.resolved();
    }
}
