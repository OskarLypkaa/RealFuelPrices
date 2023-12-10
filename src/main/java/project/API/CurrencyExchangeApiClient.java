package project.API;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import project.exceptions.APIStatusException;

public class CurrencyExchangeApiClient {
    private static final String API_URL = "http://api.exchangeratesapi.io/v1/";
    private static final String API_ACCESS_KEY = "7937cfd1f93a65a5865c4ebe9fe6aad7";

    private static final String targetCurrency = "USD,PLN";
    private static LocalDate APIDate = LocalDate.of(2004, 1, 1);
    private static LocalDate currentDate = LocalDate.now();
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

    // Method to fetch exchange rates for USD and PLN from the API
    public static Map<String, String> fetchExchangeRate() throws APIStatusException {
        HttpResponse<String> response;
        Map<String, String> exchangeRate = new HashMap<>();
        try {
            // Iterate through dates from API start date to current date
            while (APIDate.isBefore(currentDate) || APIDate.isEqual(currentDate)) {
                // Send HTTP request to the API
                response = sendHttpRequest();

                // Check if the HTTP response status code is 200 (OK)
                if (response.statusCode() == 200) {
                    // Format date and parse exchange rate from response
                    String formattedDate = APIDate.format(formatter);
                    exchangeRate.put(formattedDate, parseAndExtractExchangeRate(response.body()));
                } else {
                    // Throw an exception for non-OK status codes
                    throw new APIStatusException(
                            "Failed to fetch exchange prices. HTTP Status Code: " + response.statusCode());
                }

                // Move to the next month
                APIDate = APIDate.plusMonths(1);
            }
            System.out.println("Exchange prices API received succesfully!");
            return exchangeRate;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new APIStatusException("Failed to fetch oil prices due to an IO or InterruptedException.", e);
        }
    }

    // Method to send HTTP request to the API
    private static HttpResponse<String> sendHttpRequest()
            throws IOException, InterruptedException {
        // Create the API URL with parameters
        String urlWithParams = String.format("%s%s?access_key=%s&symbols=%s",
                API_URL, APIDate, API_ACCESS_KEY, targetCurrency);

        // Create HttpClient and HttpRequest
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlWithParams))
                .build();

        // Send the HTTP request and return the response
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // Method to parse and extract exchange rates from JSON response
    private static String parseAndExtractExchangeRate(String responseBody) {
        String result = new String();
        try {
            // Parse the JSON response
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

            // Extract USD and PLN exchange rates from the response
            double usdValue = jsonResponse.getAsJsonObject("rates").get("USD").getAsDouble();
            double plnValue = jsonResponse.getAsJsonObject("rates").get("PLN").getAsDouble();

            // Format and return the result
            result = String.format("%.5f %.5f", usdValue, plnValue);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
