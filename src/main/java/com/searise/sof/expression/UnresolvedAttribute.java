package com.searise.sof.expression;

import java.util.Optional;

public class UnresolvedAttribute implements Expression {
    public final Optional<String> db;
    public final String table;

    public UnresolvedAttribute(String table) {
        this.db = Optional.empty();
        this.table = table;
    }

    public UnresolvedAttribute(String db, String table) {
        this.db = Optional.of(db);
        this.table = table;
    }

    public String toString() {
        return "'" + db.map(s -> s + "." + table).orElse(table);
    }

    public boolean resolved() {
        return false;
    }
}
