package com;

import com.connection.StackCP;
import com.dboperations.DBOperations;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

public class Tester {
    public static void main(String[] args) throws IOException, SQLException {
        Scanner input = new Scanner(System.in);
        DBOperations db = new DBOperations();

        // Reading the connection data
        db.readConnectionData();

        int s = 0;
        while(s!=7) {
            System.out.println("Cosa vuoi fare?: ");
            System.out.println("+------------------------+-----+");
            System.out.println("| CREATE TABLES          |  1  |");
            System.out.println("| RICHIESTA PRENOTAZIONE |  2  |");
            System.out.println("| INSERT TAVOLO          |  3  |");
            System.out.println("| EXPORT PRENOTAZIONI    |  4  |");
            System.out.println("| DELETE                 |  5  |");
            System.out.println("| EXIT                   |  7  |");
            System.out.println("+------------------------+-----+");
            System.out.print("\nScelta: ");
            s = input.nextInt();
            switch (s) {
                case 1: {
                    String query = db.chooseQuery();
                    db.createTables(query);
                    break;
                }
                case 2: {
                    Prenotazione p = db.setPrenotazione();
                    db.richiestaPrenotazione(p.getCognome(), p.getData(), p.getNumeroPersone(), p.getCellulare());
                    break;
                }
                case 3: {
                    db.insertTavolo();
                    break;
                }
                case 4: {
                    db.scriviPrenotazioniSuFile();
                    break;
                }
                case 5 : {
                    db.deletePrenotazione();
                    break;
                }
                case 7: {
                    System.exit(0);
                }
            }
        }
    }
}