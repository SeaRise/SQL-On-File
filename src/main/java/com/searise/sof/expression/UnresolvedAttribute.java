package com.searise.sof.expression;

import java.util.Optional;

public class UnresolvedAttribute implements Expression {
    public final Optional<String> table;
    public final String name;

    public UnresolvedAttribute(String name) {
        this.table = Optional.empty();
        this.name = name;
    }

    public UnresolvedAttribute(String table, String name) {
        this.table = Optional.of(table);
        this.name = name;
    }

    public String toString() {
        return "'" + table.map(s -> s + "." + name).orElse(name);
    }

    public boolean resolved() {
        return false;
    }
}
