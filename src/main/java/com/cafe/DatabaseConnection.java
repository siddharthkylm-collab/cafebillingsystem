package com.cafe;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Creates JDBC connections to MySQL.
 * CHANGE the user name and password below to match YOUR MySQL installation.
 */
public class DatabaseConnection {

    private static final String DB_NAME = "cafe_db";
    private static final String DB_USER = "root";                    // <-- your MySQL username
    private static final String DB_PASSWORD = "kannan2962"; // <-- your MySQL password

    private static final String DB_URL = "jdbc:mysql://localhost:3306/" + DB_NAME
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");   // load the MySQL driver
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found. Check the pom.xml dependency.", e);
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
