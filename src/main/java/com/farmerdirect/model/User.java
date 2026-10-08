package com.farmerdirect.model;
/** Abstraction + encapsulation: common base for all roles. */
public abstract class User {
    private final long id;
    private final String name;
    protected User(long id, String name) { this.id = id; this.name = name; }
    public long getId() { return id; }
    public String getName() { return name; }
    public abstract String dashboard();   // polymorphic operation
}
