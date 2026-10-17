package com.cafe;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Database operations for the bills and bill_items tables. */
public class BillDAO {

    public static final String PENDING = "Pending";
    public static final String CONFIRMED = "Confirmed";
    public static final String CANCELLED = "Cancelled";

    /**
     * Saves a bill and all its items using ONE transaction.
     * Either everything is saved (commit) or nothing is saved (rollback).
     */
    public BillRecord saveBill(CartOrder cart, String status) throws SQLException {
        String billSql = "INSERT INTO bills (bill_number, bill_datetime, total_amount, payment_status) "
                + "VALUES (?, ?, ?, ?)";
        String itemSql = "INSERT INTO bill_items (bill_id, item_id, item_name, unit_price, quantity, subtotal) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        LocalDateTime nowFull = LocalDateTime.now();
        String billNumber = "CAFE-" + nowFull.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmssSSS"));
        LocalDateTime billTime = nowFull.withNano(0);
        double total = cart.getGrandTotal();

        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            con.setAutoCommit(false);                       // start transaction

            int billId = 0;
            try (PreparedStatement ps = con.prepareStatement(billSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, billNumber);
                ps.setTimestamp(2, Timestamp.valueOf(billTime));
                ps.setDouble(3, total);
                ps.setString(4, status);
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Could not get the new bill ID.");
                    }
                    billId = keys.getInt(1);
                }
            }

            try (PreparedStatement ps = con.prepareStatement(itemSql)) {
                for (CartItem item : cart.getItems()) {
                    ps.setInt(1, billId);
                    ps.setInt(2, item.getItemId());
                    ps.setString(3, item.getItemName());
                    ps.setDouble(4, item.getUnitPrice());
                    ps.setInt(5, item.getQuantity());
                    ps.setDouble(6, item.getSubtotal());
                    ps.executeUpdate();
                }
            }

            con.commit();                                   // save everything

            BillRecord bill = new BillRecord(billId, billNumber, billTime, total, status);
            bill.setItems(new ArrayList<>(cart.getItems()));
            return bill;

        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ignore) { }   // undo everything
            }
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException ignore) { }
            }
        }
    }

    /** Changes the payment status of a bill (Pending / Confirmed / Cancelled). */
    public void updateStatus(int billId, String newStatus) throws SQLException {
        String sql = "UPDATE bills SET payment_status = ? WHERE bill_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, billId);
            ps.executeUpdate();
        }
    }

    /** All bills, newest first (items are not loaded here). */
    public List<BillRecord> getAllBills() throws SQLException {
        List<BillRecord> list = new ArrayList<>();
        String sql = "SELECT bill_id, bill_number, bill_datetime, total_amount, payment_status "
                + "FROM bills ORDER BY bill_id DESC";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(readBill(rs));
            }
        }
        return list;
    }

    /** One bill with all its items (used for receipts). Returns null if not found. */
    public BillRecord getBillById(int billId) throws SQLException {
        String billSql = "SELECT bill_id, bill_number, bill_datetime, total_amount, payment_status "
                + "FROM bills WHERE bill_id = ?";
        String itemSql = "SELECT item_id, item_name, unit_price, quantity "
                + "FROM bill_items WHERE bill_id = ? ORDER BY bill_item_id";

        try (Connection con = DatabaseConnection.getConnection()) {
            BillRecord bill = null;
            try (PreparedStatement ps = con.prepareStatement(billSql)) {
                ps.setInt(1, billId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        bill = readBill(rs);
                    }
                }
            }
            if (bill == null) {
                return null;
            }
            List<CartItem> items = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(itemSql)) {
                ps.setInt(1, billId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        items.add(new CartItem(
                                rs.getInt("item_id"),
                                rs.getString("item_name"),
                                rs.getDouble("unit_price"),
                                rs.getInt("quantity")));
                    }
                }
            }
            bill.setItems(items);
            return bill;
        }
    }

    // ---------- Sales summary queries ----------

    public int countBills() throws SQLException {
        String sql = "SELECT COUNT(*) FROM bills";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bills WHERE payment_status = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public double sumByStatus(String status) throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM bills WHERE payment_status = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }

    private BillRecord readBill(ResultSet rs) throws SQLException {
        return new BillRecord(
                rs.getInt("bill_id"),
                rs.getString("bill_number"),
                rs.getTimestamp("bill_datetime").toLocalDateTime(),
                rs.getDouble("total_amount"),
                rs.getString("payment_status"));
    }
}
