package com.searise.sof.expression.attribute;

import java.util.Optional;

public class UnresolvedAttribute extends AttributeBase {

    public UnresolvedAttribute(String name) {
        super(name);
    }

    public UnresolvedAttribute(Optional<String> table, String name) {
        super(table, name);
    }

    public String toString() {
        return "'" + table.map(s -> s + "." + name).orElse(name);
    }

    public boolean resolved() {
        return false;
    }

    //只能用于top project.
    public static final UnresolvedAttribute UnknownUnresolvedAttribute = new UnresolvedAttribute("");
}
