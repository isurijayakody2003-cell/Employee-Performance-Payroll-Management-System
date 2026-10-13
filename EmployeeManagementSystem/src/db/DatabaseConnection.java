package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    
    // Database credentials for XAMPP MySQL
    private static final String URL = "jdbc:mysql://localhost:3306/ems_db";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Default XAMPP password is empty

    // Method to establish and return connection
    public static Connection getConnection() throws SQLException {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found.", e);
        }
    }
}