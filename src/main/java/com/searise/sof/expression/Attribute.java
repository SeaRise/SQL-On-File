package com.searise.sof.expression;

import java.util.Optional;

public class Attribute implements Expression {
    public final Optional<String> db;
    public final String table;

    public Attribute(String table) {
        this.db = Optional.empty();
        this.table = table;
    }

    public Attribute(String db, String table) {
        this.db = Optional.of(db);
        this.table = table;
    }

    public String toString() {
        return "'" + db.map(s -> s + "." + table).orElse(table);
    }
}
