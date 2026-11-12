package com.assignment.printxpress.model;

/**
 * Simple model class used to hold PrintXpress data.
 */
public class Product {
    public long id;
    public String name;
    public String category;
    public String description;
    public double basePrice;

    public Product(long id, String name, String category, String description, double basePrice) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.basePrice = basePrice;
    }
}
