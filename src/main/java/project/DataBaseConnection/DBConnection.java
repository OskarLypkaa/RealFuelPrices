package project.DataBaseConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import project.analysis.DataRunner;

public class DBConnection {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/OilApplication";
        String user = "postgres";
        String password = "superuser";

        List<Map<String, List<String>>> dataList = new ArrayList<>();

        dataList = DataRunner.fetchAllData();

        StringBuilder historicalResultString = buildQueryString(dataList, 1);
        StringBuilder currentResultString = buildQueryString(dataList, 0);

        try {
            Connection connection = DriverManager.getConnection(url, user, password);

            // Prepare INSERT query
            String query = "INSERT INTO public.historical_data_table(dates, euro_in_pln, usd_in_pln, brent_oil_in_bbls, brent_oil_in_liters, fuel_95, fuel_98, diesel, brent_oil_in_liters_in_pln) VALUES "
                    + historicalResultString + ";";
            PreparedStatement statement = connection.prepareStatement(query);

            // Execute the query
            statement.executeUpdate();

            statement.close();
            connection.close();
            System.out.println("Data has been successfully inserted into the HistoricaDataTable table.");
        } catch (SQLException e) {
            System.out.println("Error connecting to the PostgreSQL database or inserting data!");
            e.printStackTrace();
        }
        try {
            Connection connection = DriverManager.getConnection(url, user, password);

            // Prepare INSERT query
            String query = "INSERT INTO public.current_data_table(country, average_income, fuel_price, liters_month_of_work) VALUES "
                    + currentResultString + ";";
            PreparedStatement statement = connection.prepareStatement(query);

            // Execute the query
            statement.executeUpdate();

            statement.close();
            connection.close();
            System.out.println("Data has been successfully inserted into the CurrentDataTable table.");
        } catch (SQLException e) {
            System.out.println("Error connecting to the PostgreSQL database or inserting data!");
            e.printStackTrace();
        }
    }

    private static StringBuilder buildQueryString(List<Map<String, List<String>>> dataList, int index) {
        StringBuilder resultString = new StringBuilder();
        // Get the first element from the list (if it exists)
        if (!dataList.isEmpty()) {
            Map<String, List<String>> firstDataMap = dataList.get(index);

            // Loop through the lists of values in the first map
            for (String key : firstDataMap.keySet()) {
                resultString.append("(");
                resultString.append("'" + key + "'"); // Add the key in single quotes before the first value

                List<String> values = firstDataMap.get(key);
                for (String value : values) {
                    if (resultString.length() > 1) {
                        resultString.append(", ");
                    }
                    resultString.append("'" + value + "'");
                }
                resultString.append("), ");
            }

            // Remove the last space and comma
            if (resultString.length() > 1) {
                resultString.setLength(resultString.length() - 2);
            }
        } else {
            System.out.println("The list is empty.");
        }
        return resultString;
    }
}
