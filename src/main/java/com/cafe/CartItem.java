package com.cafe;

/**
 * One line in the cart or on a bill: item, unit price and quantity.
 * It stores a copy (snapshot) of the name and price, so old bills never change.
 */
public class CartItem {
    private int itemId;
    private String itemName;
    private double unitPrice;
    private int quantity;

    public CartItem(int itemId, String itemName, double unitPrice, int quantity) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public int getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public double getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }

    public void increase() { quantity++; }
    public void decrease() { if (quantity > 1) quantity--; }

    public double getSubtotal() {
        return Math.round(unitPrice * quantity * 100.0) / 100.0;
    }
}
