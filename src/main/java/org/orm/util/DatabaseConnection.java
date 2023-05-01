package org.orm.util;

import java.sql.*;

public class DatabaseConnection {

    private DatabaseConnection() {
    }

    private static Connection connection = null;

    public static Connection getConnection(){
        if (connection == null) {
            String user = "postgres";
            String url = "jdbc:postgresql://localhost:5432/dvdrental";
            String password = "password";
            try{
                connection = DriverManager.getConnection(url,user,password);
            }catch (SQLException ex){
                System.out.println(ex.getMessage());
            }
        }

        return connection;
    }
}
