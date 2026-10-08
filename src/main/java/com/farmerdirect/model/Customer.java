package com.farmerdirect.model;
public class Customer extends User {
    private final int orders;
    public Customer(long id, String name, int orders) { super(id, name); this.orders = orders; }
    @Override public String dashboard() { return "Customer " + getName() + ": " + orders + " orders placed"; }
}
