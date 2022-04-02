package com.dboperations;

import com.Prenotazione;
import com.Tavolo;
import com.connection.StackCP;
import utility.ReadProperties;

import java.io.IOException;
import java.sql.*;
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

    // This submenu let you choose which query to execute
    public String chooseQuery() throws IOException {
        int scelta;
        String query=null;
        readProperties.read("queries.properties");
        System.out.println("+---------------------------+-----+");
        System.out.println("| CREA TABELLA PRENOTAZIONE |  1  |");
        System.out.println("|   CREA TABELLA ORDINI     |  2  |");
        System.out.println("|   INSERISCI PRENOTAZIONE  |  3  |");
        System.out.println("|   INSERISCI TAVOLO        |  4  |");
        System.out.println("+---------------------------+-----+");
        scelta = input.nextInt();
        switch (scelta) {
            case 1: query = readProperties.properties.getProperty("db.create.prenotazione");break;
            case 2: query = readProperties.properties.getProperty("db.create.tavolo");break;
            case 3: query = readProperties.properties.getProperty("db.insert.prenotazione");break;
            case 4: query = readProperties.properties.getProperty("db.insert.tavolo");break;
        }
        return query;
    }

    // Used for SELECT because prints data
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

    // Used for CREATE
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

    // Helper method to get from INPUT the data for a reservation
    private Prenotazione setPrenotazione() {
        String cognome, data,cellulare;
        int numeroPersone, numeroTavolo;

        // Getting the data
        System.out.print("\nInserisci cognome: ");
        cognome=input.next();
        System.out.print("\nInserisci data: ");
        data=input.next();
        System.out.print("\nInserisci numero persone: ");
        numeroPersone=input.nextInt();
        System.out.print("\nInserisci cellulare: ");
        cellulare=input.next();
        System.out.print("\nInserisci numero tavolo: ");
        numeroTavolo=input.nextInt();

        // Building the reservation
        Prenotazione prenotazione = Prenotazione.builder()
                .cognome(cognome)
                .data(data)
                .numeroPersone(numeroPersone)
                .cellulare(cellulare)
                .numeroTavolo(numeroTavolo)
                .build();
        return prenotazione;
    }

    // Helper method to get from INPUT the data for a reservation
    private Tavolo seTavolo() {
        int numero, capienza;

        // Getting the data
        System.out.print("\nInserisci numero tavolo: ");
        numero=input.nextInt();
        System.out.print("\nInserisci capienza: ");
        capienza=input.nextInt();

        // Building the reservation
        Tavolo tavolo = Tavolo.builder()
                .numero(numero)
                .capienza(capienza)
                .build();
        return tavolo;
    }

    // This method insert data for a preservation
    public void insertPrenotazione(String query) throws SQLException {

            // Getting the num of preservation to insert into the database
            System.out.print("\nQuante prenotazioni vuoi inserire?: ");
            int n = input.nextInt();

            Connection conn = null;

            for(int i=0; i<n; i++) {
                // Calling the helper method
                Prenotazione p = setPrenotazione();
                try {
                    conn = pool.getConnection();
                    try (PreparedStatement ps = conn.prepareStatement(query)) {
                        ps.setString(1, p.getCognome());
                        ps.setString(2, p.getData());
                        ps.setInt(3, p.getNumeroPersone());
                        ps.setString(4, p.getCellulare());
                        ps.setInt(5, p.getNumeroTavolo());
                        ps.executeUpdate();
                    }
                } finally {
                    if (conn != null) {
                        pool.returnConnection(conn);
                    }
                }
            }
    }

    // This method insert data for a table reserved
    public void insertTavolo(String query) throws SQLException {

        // Getting the num of preservation to insert into the database
        System.out.print("\nQuanti tavoli vuoi inserire?: ");
        int n = input.nextInt();

        Connection conn = null;

        for(int i=0; i<n; i++) {
            // Calling the helper method
            Tavolo t = seTavolo();
            try {
                conn = pool.getConnection();
                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setInt(1, t.getNumero());
                    ps.setInt(2, t.getCapienza());
                    ps.executeUpdate();
                }
            } finally {
                if (conn != null) {
                    pool.returnConnection(conn);
                }
            }
        }
    }
}
