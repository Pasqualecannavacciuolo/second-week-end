package com.dboperations;

import com.Prenotazione;
import com.Tavolo;
import com.connection.StackCP;
import utility.ReadProperties;

import java.io.FileNotFoundException;
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


    // Used for CREATE the 2 tables
    public void createTables() throws SQLException, IOException {
        readProperties.read("queries.properties");
        String createTablePrenotazione = readProperties.properties.getProperty("db.create.prenotazione");
        String createTableTavolo = readProperties.properties.getProperty("db.create.tavolo");
        Connection conn = null;
        try {
            conn = pool.getConnection();

            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate(createTableTavolo);
                statement.executeUpdate(createTablePrenotazione);
            }
        } finally {
            if (conn != null) {
                pool.returnConnection(conn);
            }
        }
    }



    // Helper method to get data from INPUT for a reservation
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

    // Helper method to get data from INPUT for a table
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

    // Method to export all the reservation from the database to a txt file
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
