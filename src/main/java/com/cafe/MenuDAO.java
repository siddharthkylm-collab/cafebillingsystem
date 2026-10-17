package com.cafe;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Database operations for the menu_items table. */
public class MenuDAO {

    public List<MenuProduct> getAllItems() throws SQLException {
        List<MenuProduct> list = new ArrayList<>();
        String sql = "SELECT item_id, name, category, price FROM menu_items ORDER BY item_id";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new MenuProduct(
                        rs.getInt("item_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price")));
            }
        }
        return list;
    }
}
