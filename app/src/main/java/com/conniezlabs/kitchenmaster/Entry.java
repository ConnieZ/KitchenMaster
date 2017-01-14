package com.conniezlabs.kitchenmaster;

public class Entry {
    private String name;
    private String inv_qty;
    private String buy_qty;


    public Entry(String name, String inv_qty, String buy_qty) {
        this.name = name;
        this.inv_qty = inv_qty;
        this.buy_qty = buy_qty;

    }
    public String getName() {
        return name;
    }
    public String getInv_Qty() {
        return inv_qty;
    }
    public String getBuy_Qty() {
        return buy_qty;
    }

}
