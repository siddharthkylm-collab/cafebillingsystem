package com.cafe;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.print.PrinterException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.google.zxing.WriterException;

/**
 * Entry point and GUI of the Simple Cafe Management & Billing System.
 * A sidebar on the left switches between five screens (CardLayout).
 */
public class MainApplication extends JFrame {

    // ---------- Colours (light cream theme with pastel accents) ----------
    private static final Color CREAM = new Color(255, 248, 231);
    private static final Color GREEN = new Color(198, 236, 200);
    private static final Color PEACH = new Color(255, 218, 185);
    private static final Color SKY = new Color(179, 225, 247);
    private static final Color YELLOW = new Color(255, 241, 158);
    private static final Color TEXT = new Color(60, 50, 40);

    // ---------- Screen names ----------
    private static final String HOME = "Home";
    private static final String MENU = "Menu";
    private static final String ORDER = "Current Order";
    private static final String HISTORY = "Billing History";
    private static final String SUMMARY = "Sales Summary";

    // ---------- Data objects ----------
    private final MenuDAO menuDAO = new MenuDAO();
    private final BillDAO billDAO = new BillDAO();
    private final CartOrder cart = new CartOrder();

    // ---------- Layout ----------
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    // ---------- Menu screen ----------
    private JPanel productGrid;
    private JComboBox<String> categoryBox;
    private JLabel menuMessageLabel;
    private List<MenuProduct> allProducts = new ArrayList<>();
    private List<String> categoryList = new ArrayList<>();
    private boolean loadingMenu = false;

    // ---------- Current Order screen ----------
    private DefaultTableModel orderModel;
    private JTable orderTable;
    private JLabel totalLabel;

    // ---------- Billing History screen ----------
    private DefaultTableModel historyModel;
    private JTable historyTable;
    private List<BillRecord> historyBills = new ArrayList<>();

    // ---------- Sales Summary screen ----------
    private JLabel totalBillsLabel;
    private JLabel confirmedLabel;
    private JLabel pendingLabel;
    private JLabel cancelledLabel;

    public MainApplication() {
        setTitle("Simple Cafe Management & Billing System");
        setSize(1050, 680);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        contentPanel.add(createHomeScreen(), HOME);
        contentPanel.add(createMenuScreen(), MENU);
        contentPanel.add(createOrderScreen(), ORDER);
        contentPanel.add(createHistoryScreen(), HISTORY);
        contentPanel.add(createSummaryScreen(), SUMMARY);

        add(createSidebar(), BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        showScreen(HOME);
    }

    // =====================================================================
    //  Navigation
    // =====================================================================

    private void showScreen(String name) {
        // reload data from MySQL whenever a screen is opened
        if (name.equals(MENU)) {
            loadMenu();
        } else if (name.equals(ORDER)) {
            refreshOrderTable();
        } else if (name.equals(HISTORY)) {
            loadHistory();
        } else if (name.equals(SUMMARY)) {
            loadSummary();
        }
        cardLayout.show(contentPanel, name);
    }

    private JPanel createSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(GREEN);
        side.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));
        side.setPreferredSize(new Dimension(190, 0));

        JLabel logo = new JLabel("Simple Cafe");
        logo.setFont(new Font("SansSerif", Font.BOLD, 22));
        logo.setForeground(TEXT);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        side.add(logo);
        side.add(Box.createVerticalStrut(25));

        String[] names = {HOME, MENU, ORDER, HISTORY, SUMMARY};
        Color[] colors = {PEACH, SKY, YELLOW, PEACH, SKY};

        for (int i = 0; i < names.length; i++) {
            final String screenName = names[i];
            JButton button = new JButton(screenName);
            styleButton(button, colors[i]);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
            button.addActionListener(e -> showScreen(screenName));
            side.add(button);
            side.add(Box.createVerticalStrut(12));
        }
        return side;
    }

    // =====================================================================
    //  Screen 1: Home
    // =====================================================================

    private JPanel createHomeScreen() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CREAM);

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(CREAM);

        JLabel title = new JLabel("Welcome to Simple Cafe");
        title.setFont(new Font("SansSerif", Font.BOLD, 34));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Management & Billing System");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 18));
        subtitle.setForeground(TEXT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel help = new JLabel("<html><div style='text-align:center'>1. Open <b>Menu</b> and add items<br>"
                + "2. Open <b>Current Order</b> and click Checkout<br>"
                + "3. Show the UPI QR and confirm payment<br>"
                + "4. See old bills in <b>Billing History</b> and totals in <b>Sales Summary</b></div></html>");
        help.setFont(new Font("SansSerif", Font.PLAIN, 15));
        help.setForeground(TEXT);
        help.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton start = new JButton("Start New Order");
        styleButton(start, GREEN);
        start.setAlignmentX(Component.CENTER_ALIGNMENT);
        start.addActionListener(e -> showScreen(MENU));

        box.add(title);
        box.add(Box.createVerticalStrut(8));
        box.add(subtitle);
        box.add(Box.createVerticalStrut(30));
        box.add(help);
        box.add(Box.createVerticalStrut(30));
        box.add(start);

        panel.add(box);
        return panel;
    }

    // =====================================================================
    //  Screen 2: Menu
    // =====================================================================

    private JPanel createMenuScreen() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CREAM);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(CREAM);
        top.add(createTitle("Menu"), BorderLayout.WEST);

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filter.setBackground(CREAM);
        filter.add(new JLabel("Category: "));
        categoryBox = new JComboBox<>();
        categoryBox.addActionListener(e -> {
            if (!loadingMenu) {
                showProducts();
            }
        });
        filter.add(categoryBox);
        top.add(filter, BorderLayout.EAST);

        menuMessageLabel = new JLabel(" ");
        menuMessageLabel.setForeground(TEXT);
        top.add(menuMessageLabel, BorderLayout.SOUTH);

        productGrid = new JPanel(new GridLayout(0, 3, 15, 15));
        productGrid.setBackground(CREAM);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(CREAM);
        wrapper.add(productGrid, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.getViewport().setBackground(CREAM);
        scroll.setBorder(null);

        panel.add(top, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    /** Reads the menu from MySQL (so price changes in MySQL show up here). */
    private void loadMenu() {
        try {
            allProducts = menuDAO.getAllItems();
        } catch (SQLException e) {
            showError("Could not load the menu.", e);
            return;
        }

        loadingMenu = true;
        categoryList.clear();
        for (MenuProduct p : allProducts) {
            if (!categoryList.contains(p.getCategory())) {
                categoryList.add(p.getCategory());
            }
        }
        categoryBox.removeAllItems();
        categoryBox.addItem("All");
        for (String c : categoryList) {
            categoryBox.addItem(c);
        }
        loadingMenu = false;

        showProducts();
    }

    private void showProducts() {
        productGrid.removeAll();
        String selected = (String) categoryBox.getSelectedItem();
        Color[] cardColors = {GREEN, PEACH, SKY, YELLOW};

        for (MenuProduct p : allProducts) {
            if (selected == null || selected.equals("All") || selected.equals(p.getCategory())) {
                Color color = cardColors[categoryList.indexOf(p.getCategory()) % cardColors.length];
                productGrid.add(createProductCard(p, color));
            }
        }
        productGrid.revalidate();
        productGrid.repaint();
    }

    private JPanel createProductCard(MenuProduct product, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 190, 170)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JLabel name = new JLabel(product.getName());
        name.setFont(new Font("SansSerif", Font.BOLD, 17));
        name.setForeground(TEXT);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel category = new JLabel(product.getCategory());
        category.setFont(new Font("SansSerif", Font.PLAIN, 12));
        category.setForeground(TEXT);
        category.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel price = new JLabel("Rs. " + money(product.getPrice()));
        price.setFont(new Font("SansSerif", Font.BOLD, 16));
        price.setForeground(TEXT);
        price.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton add = new JButton("Add");
        styleButton(add, CREAM);
        add.setAlignmentX(Component.CENTER_ALIGNMENT);
        add.addActionListener(e -> {
            cart.addProduct(product);
            refreshOrderTable();
            menuMessageLabel.setText("Added: " + product.getName() + "   (items in order: "
                    + cart.getItems().size() + ")");
        });

        card.add(name);
        card.add(Box.createVerticalStrut(4));
        card.add(category);
        card.add(Box.createVerticalStrut(8));
        card.add(price);
        card.add(Box.createVerticalStrut(10));
        card.add(add);
        return card;
    }

    // =====================================================================
    //  Screen 3: Current Order
    // =====================================================================

    private JPanel createOrderScreen() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CREAM);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        panel.add(createTitle("Current Order"), BorderLayout.NORTH);

        String[] columns = {"Item", "Unit Price (Rs.)", "Quantity", "Subtotal (Rs.)"};
        orderModel = createReadOnlyModel(columns);
        orderTable = new JTable(orderModel);
        styleTable(orderTable);

        JScrollPane scroll = new JScrollPane(orderTable);
        scroll.getViewport().setBackground(CREAM);
        panel.add(scroll, BorderLayout.CENTER);

        // buttons for changing the order
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.setBackground(CREAM);
        JButton plus = new JButton("+ Increase");
        JButton minus = new JButton("- Decrease");
        JButton remove = new JButton("Remove Item");
        JButton clear = new JButton("Clear Order");
        styleButton(plus, GREEN);
        styleButton(minus, YELLOW);
        styleButton(remove, PEACH);
        styleButton(clear, PEACH);
        buttons.add(plus);
        buttons.add(minus);
        buttons.add(remove);
        buttons.add(clear);

        plus.addActionListener(e -> changeQuantity(1));
        minus.addActionListener(e -> changeQuantity(-1));
        remove.addActionListener(e -> {
            int row = orderTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select an item first.");
                return;
            }
            cart.removeItem(cart.getItems().get(row).getItemId());
            refreshOrderTable();
        });
        clear.addActionListener(e -> {
            cart.clear();
            refreshOrderTable();
        });

        // total and checkout
        totalLabel = new JLabel("Grand Total: Rs. 0.00");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        totalLabel.setForeground(TEXT);

        JButton checkout = new JButton("Checkout");
        styleButton(checkout, SKY);
        checkout.addActionListener(e -> checkout());

        JPanel totalRow = new JPanel(new BorderLayout(10, 0));
        totalRow.setBackground(CREAM);
        totalRow.add(totalLabel, BorderLayout.WEST);
        totalRow.add(checkout, BorderLayout.EAST);

        JPanel bottom = new JPanel(new GridLayout(2, 1, 0, 8));
        bottom.setBackground(CREAM);
        bottom.add(buttons);
        bottom.add(totalRow);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshOrderTable() {
        orderModel.setRowCount(0);
        for (CartItem item : cart.getItems()) {
            orderModel.addRow(new Object[]{
                    item.getItemName(),
                    money(item.getUnitPrice()),
                    item.getQuantity(),
                    money(item.getSubtotal())});
        }
        totalLabel.setText("Grand Total: Rs. " + money(cart.getGrandTotal()));
    }

    private void changeQuantity(int change) {
        int row = orderTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item first.");
            return;
        }
        int itemId = cart.getItems().get(row).getItemId();
        if (change > 0) {
            cart.increaseQuantity(itemId);
        } else {
            cart.decreaseQuantity(itemId);
        }
        refreshOrderTable();
        if (row < orderModel.getRowCount()) {
            orderTable.setRowSelectionInterval(row, row);
        }
    }

    private void checkout() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "The order is empty. Please add items from the Menu first.",
                    "Cannot checkout", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            BillRecord bill = billDAO.saveBill(cart, BillDAO.PENDING);   // saved to MySQL
            cart.clear();
            refreshOrderTable();
            menuMessageLabel.setText(" ");
            showPaymentDialog(bill);
        } catch (SQLException e) {
            showError("The bill could not be saved.", e);
        }
    }

    // =====================================================================
    //  Payment window (UPI QR) and receipt
    // =====================================================================

    private void showPaymentDialog(BillRecord bill) {
        final JDialog dialog = new JDialog(this, "UPI Payment - " + bill.getBillNumber(), true);
        dialog.getContentPane().setBackground(CREAM);
        dialog.setLayout(new BorderLayout(15, 15));

        // QR code on the left
        JLabel qrLabel = new JLabel("", SwingConstants.CENTER);
        qrLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 0));
        String upiLink = QRCodeGenerator.buildUpiUri(bill.getTotalAmount(), bill.getBillNumber());
        try {
            qrLabel.setIcon(new ImageIcon(QRCodeGenerator.generateQRImage(upiLink, 280)));
        } catch (WriterException e) {
            qrLabel.setText("QR code could not be generated.");
        }
        dialog.add(qrLabel, BorderLayout.WEST);

        // bill details and instructions on the right
        String message = "Bill No: " + bill.getBillNumber() + "\n"
                + "Amount to pay: Rs. " + money(bill.getTotalAmount()) + "\n"
                + "Pay to: " + QRCodeGenerator.PAYEE_NAME + " (" + QRCodeGenerator.UPI_ID + ")\n\n"
                + "How to pay:\n"
                + "1. Open Google Pay or any UPI app.\n"
                + "2. Scan this QR code.\n"
                + "3. Check that the amount matches the bill.\n"
                + "4. After the customer pays, check YOUR OWN UPI app or bank message, "
                + "then click 'Confirm Payment (Manual)'.\n\n"
                + "IMPORTANT: This app cannot check payments. Showing or scanning the QR does NOT "
                + "confirm payment. Confirmation is done manually by the cashier.";
        if (QRCodeGenerator.isPlaceholderUpiId()) {
            message += "\n\nTEST MODE: the UPI ID is still the placeholder, so a real payment will not work. "
                    + "You can still test the app by clicking 'Confirm Payment (Manual)'. "
                    + "Set UPI_ID in QRCodeGenerator.java for real use.";
        }
        JTextArea info = new JTextArea(message);
        info.setEditable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setColumns(34);
        info.setRows(17);
        info.setFont(new Font("SansSerif", Font.PLAIN, 14));
        info.setForeground(TEXT);
        info.setBackground(CREAM);
        info.setBorder(BorderFactory.createEmptyBorder(15, 5, 5, 15));
        dialog.add(info, BorderLayout.CENTER);

        // status and buttons at the bottom
        final JLabel statusLabel = new JLabel("Payment Status: " + bill.getPaymentStatus());
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        statusLabel.setForeground(TEXT);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));

        final JButton confirmButton = new JButton("Confirm Payment (Manual)");
        final JButton cancelButton = new JButton("Cancel Bill");
        JButton receiptButton = new JButton("View Receipt");
        JButton closeButton = new JButton("Close (keep as Pending)");
        styleButton(confirmButton, GREEN);
        styleButton(cancelButton, PEACH);
        styleButton(receiptButton, SKY);
        styleButton(closeButton, YELLOW);

        confirmButton.addActionListener(e -> {
            int answer = JOptionPane.showConfirmDialog(dialog,
                    "Have you checked in your own UPI app / bank that Rs. " + money(bill.getTotalAmount())
                            + " was received?\n(This app cannot verify the payment.)",
                    "Manual confirmation", JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                billDAO.updateStatus(bill.getBillId(), BillDAO.CONFIRMED);
                bill.setPaymentStatus(BillDAO.CONFIRMED);
                statusLabel.setText("Payment Status: " + bill.getPaymentStatus() + " (confirmed manually)");
                confirmButton.setEnabled(false);
                cancelButton.setEnabled(false);
            } catch (SQLException ex) {
                showError("Could not update the payment status.", ex);
            }
        });

        cancelButton.addActionListener(e -> {
            int answer = JOptionPane.showConfirmDialog(dialog, "Cancel this bill?",
                    "Cancel bill", JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                billDAO.updateStatus(bill.getBillId(), BillDAO.CANCELLED);
                bill.setPaymentStatus(BillDAO.CANCELLED);
                statusLabel.setText("Payment Status: " + bill.getPaymentStatus());
                confirmButton.setEnabled(false);
                cancelButton.setEnabled(false);
            } catch (SQLException ex) {
                showError("Could not update the payment status.", ex);
            }
        });

        receiptButton.addActionListener(e -> showReceiptDialog(bill));
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonRow.setBackground(CREAM);
        buttonRow.add(confirmButton);
        buttonRow.add(cancelButton);
        buttonRow.add(receiptButton);
        buttonRow.add(closeButton);

        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.setBackground(CREAM);
        south.add(statusLabel, BorderLayout.NORTH);
        south.add(buttonRow, BorderLayout.CENTER);
        dialog.add(south, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private String buildReceiptText(BillRecord bill) {
        String line = "--------------------------------------\n";
        StringBuilder sb = new StringBuilder();
        sb.append("             SIMPLE CAFE\n");
        sb.append(line);
        sb.append("Bill No : ").append(bill.getBillNumber()).append("\n");
        sb.append("Date    : ").append(bill.getFormattedDateTime()).append("\n");
        sb.append("Status  : ").append(bill.getPaymentStatus()).append("\n");
        sb.append(line);
        sb.append(String.format(Locale.US, "%-16s %3s %7s %9s\n", "Item", "Qty", "Price", "Amount"));
        sb.append(line);
        for (CartItem item : bill.getItems()) {
            String name = item.getItemName();
            if (name.length() > 16) {
                name = name.substring(0, 16);
            }
            sb.append(String.format(Locale.US, "%-16s %3d %7.2f %9.2f\n",
                    name, item.getQuantity(), item.getUnitPrice(), item.getSubtotal()));
        }
        sb.append(line);
        sb.append(String.format(Locale.US, "%-28s %9.2f\n", "GRAND TOTAL (Rs.)", bill.getTotalAmount()));
        sb.append(line);
        if (bill.getPaymentStatus().equals(BillDAO.PENDING)) {
            sb.append("Payment not yet confirmed.\n");
        }
        sb.append("Thank you! Visit again.\n");
        return sb.toString();
    }

    private void showReceiptDialog(BillRecord bill) {
        final JDialog dialog = new JDialog(this, "Receipt - " + bill.getBillNumber(), true);
        dialog.getContentPane().setBackground(CREAM);
        dialog.setLayout(new BorderLayout(10, 10));

        final JTextArea area = new JTextArea(buildReceiptText(bill));
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialog.add(new JScrollPane(area), BorderLayout.CENTER);

        JButton printButton = new JButton("Print");
        JButton closeButton = new JButton("Close");
        styleButton(printButton, GREEN);
        styleButton(closeButton, PEACH);

        printButton.addActionListener(e -> {
            try {
                area.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(dialog, "Could not print: " + ex.getMessage());
            }
        });
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttons.setBackground(CREAM);
        buttons.add(printButton);
        buttons.add(closeButton);
        dialog.add(buttons, BorderLayout.SOUTH);

        dialog.setSize(440, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // =====================================================================
    //  Screen 4: Billing History
    // =====================================================================

    private JPanel createHistoryScreen() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CREAM);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        panel.add(createTitle("Billing History"), BorderLayout.NORTH);

        String[] columns = {"Bill Number", "Date / Time", "Total (Rs.)", "Payment Status"};
        historyModel = createReadOnlyModel(columns);
        historyTable = new JTable(historyModel);
        styleTable(historyTable);

        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.getViewport().setBackground(CREAM);
        panel.add(scroll, BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh");
        JButton view = new JButton("View Receipt");
        JButton confirm = new JButton("Mark Confirmed (Manual)");
        JButton cancel = new JButton("Cancel Bill");
        styleButton(refresh, SKY);
        styleButton(view, GREEN);
        styleButton(confirm, YELLOW);
        styleButton(cancel, PEACH);

        refresh.addActionListener(e -> loadHistory());
        view.addActionListener(e -> {
            BillRecord selected = getSelectedHistoryBill();
            if (selected == null) {
                return;
            }
            try {
                BillRecord full = billDAO.getBillById(selected.getBillId());   // loads items too
                if (full != null) {
                    showReceiptDialog(full);
                }
            } catch (SQLException ex) {
                showError("Could not load the receipt.", ex);
            }
        });
        confirm.addActionListener(e -> changeHistoryStatus(BillDAO.CONFIRMED));
        cancel.addActionListener(e -> changeHistoryStatus(BillDAO.CANCELLED));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.setBackground(CREAM);
        buttons.add(refresh);
        buttons.add(view);
        buttons.add(confirm);
        buttons.add(cancel);
        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    private void loadHistory() {
        try {
            historyBills = billDAO.getAllBills();
        } catch (SQLException e) {
            showError("Could not load the billing history.", e);
            return;
        }
        historyModel.setRowCount(0);
        for (BillRecord bill : historyBills) {
            historyModel.addRow(new Object[]{
                    bill.getBillNumber(),
                    bill.getFormattedDateTime(),
                    money(bill.getTotalAmount()),
                    bill.getPaymentStatus()});
        }
    }

    private BillRecord getSelectedHistoryBill() {
        int row = historyTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a bill first.");
            return null;
        }
        return historyBills.get(row);
    }

    private void changeHistoryStatus(String newStatus) {
        BillRecord bill = getSelectedHistoryBill();
        if (bill == null) {
            return;
        }
        if (!bill.getPaymentStatus().equals(BillDAO.PENDING)) {
            JOptionPane.showMessageDialog(this, "Only Pending bills can be changed.");
            return;
        }
        String question = newStatus.equals(BillDAO.CONFIRMED)
                ? "Confirm that you have checked your own UPI app / bank and received Rs. "
                        + money(bill.getTotalAmount()) + "?"
                : "Cancel this bill?";
        int answer = JOptionPane.showConfirmDialog(this, question, "Please confirm",
                JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            billDAO.updateStatus(bill.getBillId(), newStatus);
            loadHistory();
        } catch (SQLException e) {
            showError("Could not update the payment status.", e);
        }
    }

    // =====================================================================
    //  Screen 5: Sales Summary
    // =====================================================================

    private JPanel createSummaryScreen() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CREAM);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        panel.add(createTitle("Sales Summary"), BorderLayout.NORTH);

        totalBillsLabel = new JLabel("-", SwingConstants.CENTER);
        confirmedLabel = new JLabel("-", SwingConstants.CENTER);
        pendingLabel = new JLabel("-", SwingConstants.CENTER);
        cancelledLabel = new JLabel("-", SwingConstants.CENTER);

        JPanel grid = new JPanel(new GridLayout(2, 2, 20, 20));
        grid.setBackground(CREAM);
        grid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        grid.add(createStatCard("Total Bills", totalBillsLabel, SKY));
        grid.add(createStatCard("Confirmed Sales", confirmedLabel, GREEN));
        grid.add(createStatCard("Pending Payments", pendingLabel, YELLOW));
        grid.add(createStatCard("Cancelled Bills", cancelledLabel, PEACH));
        panel.add(grid, BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh");
        styleButton(refresh, SKY);
        refresh.addActionListener(e -> loadSummary());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setBackground(CREAM);
        bottom.add(refresh);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 190, 170)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(TEXT);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        valueLabel.setForeground(TEXT);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    /** All numbers come from SQL queries on the bills table. */
    private void loadSummary() {
        try {
            totalBillsLabel.setText(String.valueOf(billDAO.countBills()));
            confirmedLabel.setText("Rs. " + money(billDAO.sumByStatus(BillDAO.CONFIRMED))
                    + "  (" + billDAO.countByStatus(BillDAO.CONFIRMED) + " bills)");
            pendingLabel.setText("Rs. " + money(billDAO.sumByStatus(BillDAO.PENDING))
                    + "  (" + billDAO.countByStatus(BillDAO.PENDING) + " bills)");
            cancelledLabel.setText(String.valueOf(billDAO.countByStatus(BillDAO.CANCELLED)));
        } catch (SQLException e) {
            showError("Could not load the sales summary.", e);
        }
    }

    // =====================================================================
    //  Small helper methods
    // =====================================================================

    private JLabel createTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 26));
        label.setForeground(TEXT);
        return label;
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(TEXT);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
    }

    private void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setForeground(TEXT);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(YELLOW);
        table.setSelectionForeground(TEXT);
        table.getTableHeader().setBackground(SKY);
        table.getTableHeader().setForeground(TEXT);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
    }

    private DefaultTableModel createReadOnlyModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private String money(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private void showError(String message, Exception e) {
        JOptionPane.showMessageDialog(this,
                message + "\n\n" + e.getMessage()
                        + "\n\nCheck that MySQL is running, database.sql has been run,\n"
                        + "and the username/password in DatabaseConnection.java are correct.",
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainApplication().setVisible(true));
    }
}
