package com.searise.sof.expression.attribute;

import com.searise.sof.expression.Expression;

import java.util.Optional;

public abstract class AttributeBase implements Expression {
    public final Optional<String> table;
    public final String name;

    public AttributeBase(String name) {
        this.table = Optional.empty();
        this.name = name;
    }

    public AttributeBase(Optional<String> table, String name) {
        this.table = table;
        this.name = name;
    }
}
