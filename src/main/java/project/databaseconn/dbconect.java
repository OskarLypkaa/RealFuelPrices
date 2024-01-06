package project.databaseconn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class dbconect {
    public static void main(String[] args) {
        // Dane do połączenia z bazą danych PostgreSQL
        String url = "jdbc:postgresql://localhost:5432/OilPriceDB";
        String user = "postgres";
        String password = "superuser";

        try {
            // Utworzenie połączenia
            Connection connection = DriverManager.getConnection(url, user, password);

                        
            // Zapytanie SQL do wstawienia danych do pierwszej tabeli
            String queryTable1 = "INSERT INTO DailyData (Date, Value1, Value2, Value3, Value4, Value5) VALUES (?, ?, ?, ?, ?)";
            // Utworzenie obiektu PreparedStatement
            PreparedStatement statementTable1 = connection.prepareStatement(queryTable1);
            statementTable1.setString(1, "wartosc1");
            statementTable1.setString(2, "wartosc2");
            statementTable1.setString(3, "wartosc3");
            statementTable1.setString(4, "wartosc4");
            statementTable1.setString(5, "wartosc5");
            statementTable1.executeUpdate();
            statementTable1.close();

            // Zapytanie do drugiej tabeli
            String queryTable2 = "INSERT INTO MonthlyData (Date, Value1, Value2, Value3) VALUES (?, ?, ?, ?)";
            PreparedStatement statementTable2 = connection.prepareStatement(queryTable2);
            statementTable2.setString(1, "wartosc1");
            statementTable2.setString(2, "wartosc2");
            statementTable1.setString(3, "wartosc3");
            statementTable1.setString(4, "wartosc4");
            statementTable2.executeUpdate();
            statementTable2.close();

            // Zapytanie do trzeciej tabeli
            String queryTable3 = "INSERT INTO Countries (CountryName, OilPrice) VALUES (?, ?)";
            PreparedStatement statementTable3 = connection.prepareStatement(queryTable3);
            statementTable3.setString(1, "wartosc1");
            statementTable3.setString(2, "wartosc1");
            statementTable3.executeUpdate();
            statementTable3.close();

            // Zamknięcie zasobów
            connection.close();
        } catch (SQLException e) {
            System.out.println("Błąd połączenia z bazą danych PostgreSQL!");
            e.printStackTrace();
        }
    }
}
