package com.cafe;

/** One item on the cafe menu (a row of the menu_items table). */
public class MenuProduct {
    private int id;
    private String name;
    private String category;
    private double price;

    public MenuProduct(int id, String name, String category, double price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
}
