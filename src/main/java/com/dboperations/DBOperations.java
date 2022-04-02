package com.dboperations;

import com.connection.StackCP;
import utility.ReadProperties;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class DBOperations {
    Scanner input = new Scanner(System.in);

    // Reading the connection parameters from file
    ReadProperties readProperties = new ReadProperties();

    // Variables to instance a ConnectionPool
    String databaseUrl;
    String userName;
    String password;
    int maxPoolSize = 10;   // Number of connection between active and idling
    StackCP pool;

    // Reading database connection data from properties file
    public void readConnectionData() throws IOException {
        readProperties.read("application.properties");
        this.databaseUrl = readProperties.properties.getProperty("db.url");
        this.userName = readProperties.properties.getProperty("db.username");
        this.password = readProperties.properties.getProperty("db.password");
        // Instantiating ConnectionPool Object with mysql parameters taken from application.properties
        pool = new StackCP(databaseUrl, userName, password, maxPoolSize);
    }

    public String chooseQuery() throws IOException {
        int scelta;
        String query=null;
        readProperties.read("queries.properties");
        System.out.println("|CREA TABELLA PRENOTAZIONE |  1  |");
        System.out.println("|   CREA TABELLA ORDINI    |  2  |");
        scelta = input.nextInt();
        switch (scelta) {
            case 1: query = readProperties.properties.getProperty("db.create.prenotazione");break;
            case 2: query = readProperties.properties.getProperty("db.create.tavolo");break;
        }
        return query;
    }

    public void tryExecute(String query) throws SQLException {
        Connection conn = null;
        try {
            conn = pool.getConnection();

            try (Statement statement = conn.createStatement()) {
                ResultSet res = statement.executeQuery(query);
                while (res.next()) {
                    String tblName = res.getString(1);
                    System.out.println(tblName);
                }
            }
        } finally {
            if (conn != null) {
                pool.returnConnection(conn);
            }
        }
    }

    public void tryExecuteUpdate(String query) throws SQLException{
        Connection conn = null;
        try {
            conn = pool.getConnection();

            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate(query);
            }
        } finally {
            if (conn != null) {
                pool.returnConnection(conn);
            }
        }
    }
}
