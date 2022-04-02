package com;

import com.dboperations.DBOperations;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

public class Tester {
    public static void main(String[] args) throws IOException, SQLException {
        Scanner input = new Scanner(System.in);
        DBOperations db = new DBOperations();

        db.readConnectionData();

        int s = 0;
        while(s!=6) {
            System.out.println("Cosa vuoi fare?: ");
            System.out.println("+--------+-----+");
            System.out.println("| CREATE |  1  |");
            System.out.println("| INSERT |  2  |");
            System.out.println("| SELECT |  3  |");
            System.out.println("| UPDATE |  4  |");
            System.out.println("| DELETE |  5  |");
            System.out.println("|  EXIT  |  6  |");
            System.out.println("+--------+-----+");
            System.out.print("\nScelta: ");
            s = input.nextInt();
            switch (s) {
                case 1: {
                    String query = db.chooseQuery();
                    db.tryExecuteUpdate(query);
                    break;
                }
                case 2: {
                    String query = db.chooseQuery();
                    db.insertPrenotazione(query);
                    String query2 = db.chooseQuery();
                    db.insertTavolo(query2);
                }
                case 6: {
                    System.exit(0);
                }
            }
        }
    }
}