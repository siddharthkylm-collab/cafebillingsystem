package com.cafe;

import java.util.ArrayList;
import java.util.List;

/** The current order (cart): add/remove items, change quantities, calculate total. */
public class CartOrder {
    private List<CartItem> items = new ArrayList<>();

    public void addProduct(MenuProduct product) {
        for (CartItem item : items) {
            if (item.getItemId() == product.getId()) {
                item.increase();           // already in cart -> just add one more
                return;
            }
        }
        items.add(new CartItem(product.getId(), product.getName(), product.getPrice(), 1));
    }

    public void increaseQuantity(int itemId) {
        for (CartItem item : items) {
            if (item.getItemId() == itemId) {
                item.increase();
                return;
            }
        }
    }

    public void decreaseQuantity(int itemId) {
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            if (item.getItemId() == itemId) {
                if (item.getQuantity() > 1) {
                    item.decrease();
                } else {
                    items.remove(i);       // quantity would become 0 -> remove the line
                }
                return;
            }
        }
    }

    public void removeItem(int itemId) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getItemId() == itemId) {
                items.remove(i);
                return;
            }
        }
    }

    public void clear() { items.clear(); }

    public boolean isEmpty() { return items.isEmpty(); }

    public List<CartItem> getItems() { return items; }

    public double getGrandTotal() {
        double total = 0;
        for (CartItem item : items) {
            total += item.getSubtotal();
        }
        return Math.round(total * 100.0) / 100.0;
    }
}
