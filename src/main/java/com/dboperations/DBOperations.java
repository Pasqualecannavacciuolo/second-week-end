package com.dboperations;

import com.Prenotazione;
import com.Tavolo;
import com.connection.StackCP;
import utility.ReadProperties;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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



    // This submenu let you choose which CREATE OPERATION to execute
    public String chooseQuery() throws IOException {
        int scelta;
        String query = null;
        readProperties.read("queries.properties");
        System.out.println("+---------------------------+-----+");
        System.out.println("| CREA TABELLA PRENOTAZIONE |  1  |");
        System.out.println("|   CREA TABELLA ORDINI     |  2  |");
        System.out.println("|   INSERISCI PRENOTAZIONE  |  3  |");
        System.out.println("|   INSERISCI TAVOLO        |  4  |");
        System.out.println("+---------------------------+-----+");
        scelta = input.nextInt();
        switch (scelta) {
            case 1:
                query = readProperties.properties.getProperty("db.create.prenotazione");
                break;
            case 2:
                query = readProperties.properties.getProperty("db.create.tavolo");
                break;
        }
        return query;
    }

    // Used for CREATE
    public void createTables(String query) throws SQLException {
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
    public Prenotazione setPrenotazione() {
        String cognome, data, cellulare;
        int numeroPersone;

        // Getting the data
        System.out.print("\nInserisci cognome: ");
        cognome = input.next();
        System.out.print("\nInserisci data: ");
        data = input.next();
        System.out.print("\nInserisci numero persone: ");
        numeroPersone = input.nextInt();
        System.out.print("\nInserisci cellulare: ");
        cellulare = input.next();

        // Building the reservation
        Prenotazione prenotazione = Prenotazione.builder()
                .cognome(cognome)
                .data(data)
                .numeroPersone(numeroPersone)
                .cellulare(cellulare)
                .build();
        return prenotazione;
    }

    // Helper method to get from INPUT the data for a reservation
    private Tavolo setTavolo() {
        int numero, capienza;

        // Getting the data
        System.out.print("\nInserisci numero tavolo: ");
        numero = input.nextInt();
        System.out.print("\nInserisci capienza: ");
        capienza = input.nextInt();

        // Building the reservation
        Tavolo tavolo = Tavolo.builder()
                .numero(numero)
                .capienza(capienza)
                .build();
        return tavolo;
    }



    // This method insert data for a preservation
    public void insertPrenotazione() throws SQLException, IOException {
        readProperties.read("queries.properties");
        String query = readProperties.properties.getProperty("db.insert.prenotazione");

        // Getting the num of preservation to insert into the database
        System.out.print("\nQuante prenotazioni vuoi inserire?: ");
        int n = input.nextInt();

        Connection conn = null;

        for (int i = 0; i < n; i++) {
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
    public void insertTavolo() throws SQLException, IOException {
        readProperties.read("queries.properties");
        String query = readProperties.properties.getProperty("db.insert.tavolo");
        // Getting the num of preservation to insert into the database
        System.out.print("\nQuanti tavoli vuoi inserire?: ");
        int n = input.nextInt();

        Connection conn = null;

        for (int i = 0; i < n; i++) {
            // Calling the helper method
            Tavolo t = setTavolo();
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



    // This method delete a reservation and automatically deletes the reserved table
    public void deletePrenotazione() throws SQLException, IOException {
        Connection conn = null;
        readProperties.read("queries.properties");
        String query = readProperties.properties.getProperty("db.delete.prenotazione");

        String cognome, data;
        System.out.print("\nCognome da cancellare: ");
        cognome = input.next();
        System.out.print("\nData: ");
        data = input.next();

        try {
            conn = pool.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, cognome);
                ps.setString(2, data);
                ps.executeUpdate();
            }
        } finally {
            if (conn != null) {
                pool.returnConnection(conn);
            }
        }
    }



    /**
     * This method use the output from the helper method disponibilitàTavolo if the output is not equal to null
     * then we proceed to insert a new reservation with a table that can hold the number of people of the reservation
     *
     * @param cognome
     * @param data
     * @param numeroPersone
     * @param cellulare
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public boolean richiestaPrenotazione(String cognome, String data, int numeroPersone, String cellulare) throws SQLException, IOException {
        // Checking if a table can hold the number of person of this preservation
        String _numeroTavolo = disponibilitàTavolo(numeroPersone);
        Connection conn = null;
        ReadProperties readProperties = new ReadProperties();
        readProperties.read("queries.properties");
        if (_numeroTavolo != null) {
            try {
                conn = pool.getConnection();
                try (PreparedStatement ps = conn.prepareStatement(readProperties.properties.getProperty("db.insert.prenotazione"))) {
                    ps.setString(1, cognome);
                    ps.setString(2, data);
                    ps.setInt(3, numeroPersone);
                    ps.setString(4, cellulare);
                    ps.setInt(5, Integer.parseInt(_numeroTavolo));
                    ps.executeUpdate();
                }
            } finally {
                if (conn != null) {
                    pool.returnConnection(conn);
                }
            }
        }
        return false;
    }

    // Helper method to get the numberTable of a table that can hold the capacity of people of the preservation
    public String disponibilitàTavolo(int numeroPersone) throws SQLException {
        String query = "SELECT Numero FROM `Ristorante`.`Tavolo` WHERE Capienza >=" + numeroPersone + ";";
        Connection conn = null;
        String numeroTavolo = null;
        try {
            conn = pool.getConnection();

            try (Statement statement = conn.createStatement()) {
                ResultSet res = statement.executeQuery(query);
                while (res.next()) {
                    numeroTavolo = res.getString(1);
                }
            }
        } finally {
            if (conn != null) {
                pool.returnConnection(conn);
            }
        }
        return numeroTavolo;
    }

    public void scriviPrenotazioniSuFile() throws IOException, SQLException{
        String query = "SELECT * FROM Prenotazione";
        Connection conn = null;

        String cognome, data, numeroPersone, cellulare;
        int numeroTavolo;

        try {
            PrintWriter outputStream = new PrintWriter("prenotazioni.txt");
            outputStream.println(String.format("%-20s %-20s %-20s %-20s %-20s", "COGNOME", "DATA", "NUMERO PERSONE", "CELLULARE", "NUMERO TAVOLO"));


            try {
                conn = pool.getConnection();

                try (Statement statement = conn.createStatement()) {
                    ResultSet res = statement.executeQuery(query);
                    while (res.next()) {
                        cognome = res.getString(1);
                        data = res.getString(2);
                        numeroPersone = res.getString(3);
                        cellulare = res.getString(4);
                        numeroTavolo = res.getInt(5);
                        outputStream.println(String.format("%-20s %-20s %-20s %-20s %-20s", cognome, data, numeroPersone, cellulare, numeroTavolo));
                    }
                }
            } finally {
                if (conn != null) {
                    pool.returnConnection(conn);
                }
            }


            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
