-- Simple Cafe Management & Billing System
-- Run this file once in MySQL to create the database, tables and menu data.

CREATE DATABASE IF NOT EXISTS cafe_db;
USE cafe_db;

-- 1. Menu items (edit prices here with an UPDATE statement)
CREATE TABLE IF NOT EXISTS menu_items (
    item_id  INT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(50)   NOT NULL UNIQUE,
    category VARCHAR(30)   NOT NULL,
    price    DECIMAL(8,2)  NOT NULL
);

-- 2. Bills (one row per bill)
CREATE TABLE IF NOT EXISTS bills (
    bill_id        INT AUTO_INCREMENT PRIMARY KEY,
    bill_number    VARCHAR(30)    NOT NULL UNIQUE,
    bill_datetime  DATETIME       NOT NULL,
    total_amount   DECIMAL(10,2)  NOT NULL,
    payment_status ENUM('Pending','Confirmed','Cancelled') NOT NULL DEFAULT 'Pending'
);

-- 3. Bill items (many rows per bill)
CREATE TABLE IF NOT EXISTS bill_items (
    bill_item_id INT AUTO_INCREMENT PRIMARY KEY,
    bill_id      INT            NOT NULL,
    item_id      INT            NOT NULL,
    item_name    VARCHAR(50)    NOT NULL,   -- name snapshot at time of sale
    unit_price   DECIMAL(8,2)   NOT NULL,   -- price snapshot at time of sale
    quantity     INT            NOT NULL,
    subtotal     DECIMAL(10,2)  NOT NULL,
    FOREIGN KEY (bill_id) REFERENCES bills(bill_id),
    FOREIGN KEY (item_id) REFERENCES menu_items(item_id)
);

-- Initial menu (INSERT IGNORE: safe to run the script again)
INSERT IGNORE INTO menu_items (name, category, price) VALUES
('Tea',              'Hot Drinks',  10),
('Coffee',           'Hot Drinks',  20),
('Cappuccino',       'Hot Drinks',  40),
('Lemon Tea',        'Hot Drinks',  20),
('Cold Coffee',      'Cold Drinks', 60),
('Fresh Lime Juice', 'Cold Drinks', 30),
('Milkshake',        'Cold Drinks', 70),
('Sandwich',         'Snacks',      60),
('Veg Burger',       'Snacks',      80),
('French Fries',     'Snacks',      50),
('Veg Pizza',        'Snacks',     120),
('Chocolate Cake',   'Desserts',    50);
