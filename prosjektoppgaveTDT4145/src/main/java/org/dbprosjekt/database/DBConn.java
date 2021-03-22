package org.dbprosjekt.database;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConn {
    public Connection conn;

    public DBConn() {
        connect();
    }

    public void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            var dbConnString = new StringBuilder();

            dbConnString.append("jdbc:mysql://79.160.125.77:3306/");
            dbConnString.append("halvor_prosjekt");
            dbConnString.append("?useSSL=false");
            dbConnString.append("&allowPublicKeyRetrieval=true");
            dbConnString.append("&serverTimezone=Europe/Oslo");

            System.out.println(dbConnString);
            conn = DriverManager.getConnection(dbConnString.toString(), "halvor", "O^o]FcUwpHbIQ=^KXL!%HW6I");
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException("Unable to connect", e);
        }
    }
}