package com.conniezlabs.kitchenmaster;

public class Entry {
    private String id;
    private String name;
    private int inv_qty;
    private int buy_qty;


    public Entry(String name, int inv_qty, int buy_qty) {
        this.name = name;
        this.inv_qty = inv_qty;
        this.buy_qty = buy_qty;
    }

    public Entry(String id, String name, int inv_qty, int buy_qty) {
        this.id = id;
        this.name = name;
        this.inv_qty = inv_qty;
        this.buy_qty = buy_qty;

    }
    public String getId(){ return id; }
    public String getName() {
        return name;
    }
    public String getInv_Qty() {
        if(inv_qty >0)
            return ""+inv_qty;
        else
            return "";
    }
    public String getBuy_Qty() {
        if(buy_qty >0)
            return ""+buy_qty;
        else
            return "";
    }

}
