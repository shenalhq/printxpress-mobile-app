package com.assignment.printxpress.model;

/**
 * Simple model class used to hold PrintXpress data.
 */
public class CartItem {
    public long id;
    public long productId;
    public String productName;
    public int quantity;
    public double unitPrice;
    public String size;
    public String material;
    public String finish;
    public String instructions;
    public String artworkUri;
    public String fulfillment;

    public double lineTotal() { return unitPrice * quantity; }
}
