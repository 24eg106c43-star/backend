package com.farmerdirect.model;
public class Admin extends User {
    private final int farmers, customers, products, orders;
    public Admin(int farmers, int customers, int products, int orders) { super(0, "Admin"); this.farmers = farmers; this.customers = customers; this.products = products; this.orders = orders; }
    @Override public String dashboard() { return "System: " + farmers + " farmers, " + customers + " customers, " + products + " products, " + orders + " orders"; }
}
