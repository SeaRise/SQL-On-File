package com.searise.sof.expression;

public class Attribute extends UnresolvedAttribute {
    public Attribute(String db, String table) {
        super(db, table);
    }

    public boolean resolved() {
        return true;
    }
}
