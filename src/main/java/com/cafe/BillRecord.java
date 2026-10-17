package com.cafe;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** A saved bill (a row of the bills table) together with its items. */
public class BillRecord {
    private int billId;
    private String billNumber;
    private LocalDateTime billDateTime;
    private double totalAmount;
    private String paymentStatus;
    private List<CartItem> items = new ArrayList<>();

    public BillRecord(int billId, String billNumber, LocalDateTime billDateTime,
                      double totalAmount, String paymentStatus) {
        this.billId = billId;
        this.billNumber = billNumber;
        this.billDateTime = billDateTime;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
    }

    public int getBillId() { return billId; }
    public String getBillNumber() { return billNumber; }
    public LocalDateTime getBillDateTime() { return billDateTime; }
    public double getTotalAmount() { return totalAmount; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public String getFormattedDateTime() {
        return billDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }
}
