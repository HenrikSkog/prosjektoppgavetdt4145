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
            //Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            var dbConnString = new StringBuilder();

            dbConnString.append("jdbc:mysql://10.10.105.97:3306/");
            dbConnString.append("prosjektoppgavetdt4145");
            dbConnString.append("?useSSL=false");
            dbConnString.append("&allowPublicKeyRetrieval=true");
            dbConnString.append("&serverTimezone=Europe/Oslo");


            conn = DriverManager.getConnection(dbConnString.toString(), "root", "pAssord123");
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException("Unable to connect", e);
        }
    }
}