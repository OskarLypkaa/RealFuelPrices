package project.API;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import project.exceptions.APIStatusException;

public class CurrencyExchangeApiClient {
    private static final String API_URL = "http://api.exchangeratesapi.io/v1/";
    private static final String API_ACCESS_KEY = "7937cfd1f93a65a5865c4ebe9fe6aad7";

    private static final String targetCurrency = "USD,PLN";
    private String dateForAPI = "1999-02-01";

    public Map<String, Double> fetchExchangeRate() throws APIStatusException {
        try {
            HttpResponse<String> response = sendHttpRequest();
            if (response.statusCode() == 200) {
                Map<String, Double> exchangeRate = new HashMap<>();
                exchangeRate.put(, );

                for (Map.Entry<String, Double> entry : exchangeRate.entrySet()) {
                    String period = entry.getKey();
                    double value = entry.getValue();
                    System.out.println("Date: " + period + ", Value: " + value);
                }
                return parseAndExtractExchangeRate(response.body());
            } else
                throw new APIStatusException("Failed to fetch oil prices. HTTP Status Code: " + response.statusCode());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new APIStatusException("Failed to fetch oil prices due to an IO or InterruptedException.", e);
        }
    }

    private HttpResponse<String> sendHttpRequest()
            throws IOException, InterruptedException {
        String urlWithParams = String.format("%s%s?access_key=%s&symbols=%s",
                API_URL, dateForAPI, API_ACCESS_KEY, targetCurrency);

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlWithParams))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String parseAndExtractExchangeRate(String responseBody) {
        Map<String, Double> result = new LinkedHashMap<>();

        try {
            // Parse the JSON response
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonObject responseObject = jsonResponse.getAsJsonObject("response");

            // Check if the "data" key exists in the JSON response
            if (responseObject.has("data")) {
                // Extract the "data" element from the JSON response
                JsonElement dataElement = responseObject.get("data");

                // Check if "data" is a JsonArray
                if (dataElement.isJsonArray()) {
                    JsonArray dataArray = dataElement.getAsJsonArray();

                    // Iterate through each entry in the "data" array
                    for (int i = 0; i < dataArray.size(); i++) {
                        // Extract information for each data entry
                        JsonObject dataEntry = dataArray.get(i).getAsJsonObject();
                        String period = dataEntry.get("period").getAsString();
                        double value = dataEntry.get("value").getAsDouble();

                        // Put the extracted information into the result map
                        result.put(period, value);
                    }
                } else {
                    // Handle the case where "data" is a JsonObject
                    // You might want to log a warning or handle it accordingly based on your
                    // requirements
                    System.out.println("Unexpected JSON structure: 'data' is not an array.");
                }
            } else {
                // Handle the case where the key "data" is not present in the JSON response
                System.out.println("JSON response does not contain the 'data' key.");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Handle the exception appropriately
        }

        return result;
    }

}
