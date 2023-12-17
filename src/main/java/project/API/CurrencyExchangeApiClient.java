package project.API;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import project.exceptions.APIStatusException;

public class CurrencyExchangeApiClient {
    private static final String API_URL = "http://api.nbp.pl/api/exchangerates/rates/a/";

    private static LocalDate currentDate = LocalDate.now();
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Method to fetch exchange rates for EUR and USD from the API
    public static Map<String, List<String>> fetchExchangeRate() throws APIStatusException {
        LocalDate APIDate = LocalDate.of(2022, 4, 1);
        HttpResponse<String> response;
        Map<String, List<String>> exchangeRate = new HashMap<>();
        Map<String, String> oneYearExchangeRateMap = new HashMap<>();
        List<String> oneYearExchangeRateList = new ArrayList<>();

        try {

            // Iterate through dates from API start date to current date
            while (APIDate.getYear() <= currentDate.getYear()) {
                // Time sleep for API not to receive status code: 429
                Thread.sleep(1000);

                // Send HTTP request to the API for EUR
                response = sendHttpRequest("eur", APIDate);

                // Check if the HTTP response status code is 200 (OK)
                if (response.statusCode() == 200) {

                    // Format date and parse exchange rate from response for EUR
                    oneYearExchangeRateMap = parseAndExtractExchangeRate(response.body());

                    while (!APIDate.isEqual(APIDate.plusYears(1)) || !APIDate.isEqual(currentDate)) {
                        if (oneYearExchangeRateMap.get(APIDate.toString()) != null)
                            oneYearExchangeRateList.add(oneYearExchangeRateMap.get(APIDate.toString()));
                        else {
                            LocalDate tempAPIDate = APIDate;
                            while (tempAPIDate.toString() == null) {
                                String valueDayBefore = oneYearExchangeRateMap.get(tempAPIDate.minusDays(1).toString());
                                String valueDayAfter = oneYearExchangeRateMap.get(tempAPIDate.plusDays(1).toString());
                                Double avrValue = Double.parseDouble(valueDayAfter) * Double.parseDouble(valueDayBefore)
                                        / 2;
                                oneYearExchangeRateList.add(avrValue.toString());
                            }
                        }

                        exchangeRate.put(APIDate.toString(), oneYearExchangeRateList);
                        APIDate = APIDate.plusDays(1);
                    }

                } else {
                    // Throw an exception for non-OK status codes
                    throw new APIStatusException(
                            "Failed to fetch exchange prices. HTTP Status Code: " + response.statusCode());
                }
                System.out.println("Request succesfull!");
                // Move to the next day
                APIDate = APIDate.plusDays(1);
            }
            System.out.println("Exchange prices API received successfully!");
            return exchangeRate;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new APIStatusException("Failed to fetch exchanged rates due to IO or InterruptedException.", e);
        }
    }

    // Method to send HTTP request to the API
    private static HttpResponse<String> sendHttpRequest(String currencyCode, LocalDate startingDate)
            throws IOException, InterruptedException {

        String urlWithParams;
        // Create the API URL with parameters
        if (startingDate.getYear() != currentDate.getYear()) {
            String nextYearDate = startingDate.plusYears(1).format(formatter);
            urlWithParams = String.format("%s%s/%s/%s/?format=json", API_URL, currencyCode,
                    startingDate.format(formatter), nextYearDate);
        } else {
            urlWithParams = String.format("%s%s/%s/%s/?format=json", API_URL, currencyCode,
                    startingDate.format(formatter), currentDate);
        }

        // Create HttpClient and HttpRequest
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlWithParams)).build();

        // Send the HTTP request and return the response
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // Method to parse and extract exchange rates from JSON response
    private static Map<String, String> parseAndExtractExchangeRate(String responseBody) {
        Map<String, String> result = new HashMap<>();
        try {
            // Parse the JSON response
            JsonArray jsonArray = JsonParser.parseString(responseBody).getAsJsonObject().getAsJsonArray("rates");

            // Extract PLN exchange rate from the response
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject rate = jsonArray.get(i).getAsJsonObject();
                String effectiveDate = rate.get("effectiveDate").getAsString();
                String midValue = rate.get("mid").getAsString();
                result.put(effectiveDate, midValue);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
