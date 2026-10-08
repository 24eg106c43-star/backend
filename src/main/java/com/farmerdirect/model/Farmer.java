package com.farmerdirect.model;
public class Farmer extends User {
    private final int listings, orders;
    public Farmer(long id, String name, int listings, int orders) { super(id, name); this.listings = listings; this.orders = orders; }
    @Override public String dashboard() { return "Farmer " + getName() + ": " + listings + " crops listed, " + orders + " orders received"; }
}
