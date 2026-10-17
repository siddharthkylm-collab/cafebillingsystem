# Simple Cafe Management & Billing System

KTU B.Tech Java project - Java Swing GUI, MySQL (JDBC), ZXing UPI QR code.

## Project structure

```
cafe-management/
  pom.xml
  database.sql
  README.md
  src/main/java/com/cafe/
    MainApplication.java    - entry point + all GUI screens
    MenuProduct.java        - menu item model
    CartItem.java           - one line in the cart / on a bill
    CartOrder.java          - cart, quantities, total
    BillRecord.java         - a saved bill (with its items)
    DatabaseConnection.java - MySQL JDBC connection
    MenuDAO.java            - reads menu_items
    BillDAO.java            - saves/reads bills and bill_items
    QRCodeGenerator.java    - UPI link + QR image (ZXing)
```

## 1. Install the tools (Windows)

1. **JDK 17 or newer** (e.g. Eclipse Temurin 17 or 21). Check: `java -version` and `javac -version`.
2. **Apache Maven**: download the zip from maven.apache.org, extract it, and add its `bin` folder to the Windows PATH. Check: `mvn -version`.
3. **MySQL Server 8** (MySQL Installer for Windows). Remember the `root` password you set. Make sure the MySQL service is running.
4. **VS Code** with the *Extension Pack for Java* (Microsoft).

## 2. Create the database

Open a terminal in the project folder (VS Code: Terminal > New Terminal), then run:

```
mysql -u root -p -e "source database.sql"
```

(If `mysql` is not recognised, add `C:\Program Files\MySQL\MySQL Server 8.0\bin` to PATH, or open `database.sql` in MySQL Workbench and run it.)

Check it worked:

```
mysql -u root -p -e "USE cafe_db; SELECT * FROM menu_items;"
```

### Changing menu prices

Prices live in MySQL, for example:

```
mysql -u root -p -e "USE cafe_db; UPDATE menu_items SET price = 25 WHERE name = 'Coffee';"
```

Open the Menu screen again in the app and the new price appears.

## 3. Configure the project

Open `src/main/java/com/cafe/DatabaseConnection.java` and set:

```java
private static final String DB_NAME = "cafe_db";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "YOUR_MYSQL_PASSWORD";
```

Open `src/main/java/com/cafe/QRCodeGenerator.java` and set the cafe UPI details:

```java
public static final String UPI_ID = "yourcafe@upi";
public static final String PAYEE_NAME = "Simple Cafe";
```

With the placeholder UPI ID the app runs in **test mode**: you can test everything without a real payment.

## 4. Build and run

In the VS Code terminal, inside the project folder:

```
mvn clean compile
mvn exec:java
```

The first run downloads the MySQL driver and ZXing from Maven Central (internet needed once).

## 5. How to demonstrate (viva)

1. Show `database.sql` and the three tables in MySQL (menu_items, bills, bill_items).
2. Open **Menu**, filter by category, click **Add** on a few items.
3. Open **Current Order**: use + / - / Remove, watch the grand total change. Click **Checkout** with an empty cart to show the warning.
4. Add items and **Checkout**: the bill is saved to MySQL (as a transaction) with status *Pending*, and the UPI QR window opens.
5. Click **Confirm Payment (Manual)** - the status becomes *Confirmed*. Explain that the app cannot verify payment; a person confirms it.
6. Click **View Receipt** and **Print**.
7. Open **Billing History** (real rows from MySQL), view a receipt, mark a Pending bill Confirmed or Cancelled.
8. Open **Sales Summary** (COUNT/SUM queries). Close and restart the app to show that data is still there.

## How the classes connect

`MainApplication` (GUI) -> uses `MenuDAO` to read the menu -> shows `MenuProduct` cards -> adds them to `CartOrder` (which holds `CartItem`s) -> on Checkout `BillDAO.saveBill()` writes `bills` + `bill_items` in one transaction and returns a `BillRecord` -> `QRCodeGenerator` makes the UPI QR for that bill. `MenuDAO` and `BillDAO` get their connections from `DatabaseConnection`.

## Troubleshooting

| Problem | Fix |
|---|---|
| `Access denied for user 'root'` | Wrong username/password in `DatabaseConnection.java`. |
| `Communications link failure` / `Connection refused` | MySQL service is not running (Windows: Services > MySQL80 > Start), or port is not 3306. |
| `Unknown database 'cafe_db'` | `database.sql` was not run. Run step 2. |
| `Table ... doesn't exist` | Re-run `database.sql`. |
| `MySQL JDBC driver not found` | Run `mvn clean compile` again with internet on; check the `mysql-connector-j` dependency in `pom.xml`. |
| `Public Key Retrieval is not allowed` | Already handled by `allowPublicKeyRetrieval=true` in the JDBC URL. |
| `mvn` / `mysql` not recognised | Add Maven's `bin` / MySQL's `bin` folder to PATH, then reopen the terminal. |
| `invalid target release: 17` | Install JDK 17+ and make sure `JAVA_HOME` points to it. |
| Menu screen shows an error | Same causes as above - the message tells you the SQL error. |
