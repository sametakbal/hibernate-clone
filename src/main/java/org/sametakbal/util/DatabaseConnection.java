package org.sametakbal.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    public Connection connect() {
        Connection conn = null;
        try {
            String url = "jdbc:postgresql://localhost:5432/dvdrental";
            String user = "postgres";
            String password = "password";
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }
}