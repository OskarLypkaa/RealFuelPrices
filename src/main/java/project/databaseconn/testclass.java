package project.databaseconn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class testclass {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/OilPriceDB";
        String user = "postgres";
        String password = "superuser";

        // Przykładowa mapa z danymi do wstawienia
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("Date", java.sql.Date.valueOf("2023-12-31"));
        dataMap.put("Value1", "SampleValue1");
        dataMap.put("Value2", "SampleValue2");
        dataMap.put("Value3", "SampleValue3");
        // ... Dodaj więcej danych do mapy, w zależności od potrzeb

        try {
            Connection connection = DriverManager.getConnection(url, user, password);

            // Generowanie dynamicznego zapytania INSERT na podstawie kluczy z mapy
            StringBuilder columns = new StringBuilder();
            StringBuilder values = new StringBuilder();
            for (String key : dataMap.keySet()) {
                if (columns.length() > 0) {
                    columns.append(", ");
                    values.append(", ");
                }
                columns.append(key);
                values.append("?");
            }

            // Przygotowanie zapytania INSERT
            String query = "INSERT INTO testtwo (" + columns + ") VALUES (" + values + ")";
            PreparedStatement statement = connection.prepareStatement(query);

            // Ustawianie wartości parametrów z mapy
            int index = 1;
            for (Object value : dataMap.values()) {
                if (value instanceof java.sql.Date) {
                    statement.setDate(index, (java.sql.Date) value);
                } else {
                    statement.setObject(index, value);
                }
                index++;
            }

            // Wykonanie zapytania
            statement.executeUpdate();

            statement.close();
            connection.close();
            System.out.println("Dane zostały pomyślnie wstawione do tabeli testtwo.");
        } catch (SQLException e) {
            System.out.println("Błąd połączenia z bazą danych PostgreSQL lub podczas wstawiania danych!");
            e.printStackTrace();
        }
    }
}
