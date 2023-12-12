package project.API;

import project.exceptions.APIStatusException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

public class OilPriceApiClient {
    private static final String API_URL = "https://api.eia.gov/v2/petroleum/pri/spt/data/";
    private static final String API_TOKEN = "?api_key=1hU4hrUQ8qs1uR4L9UScdgCAqhDLNRBAmg9cchbv";
    private static final String API_PARAMETERS = "&frequency=monthly&data[0]=value&facets[series][]=RBRTE&start=2004-01&sort[0][column]=period&sort[0][direction]=desc&offset=0&length=5000";

    // Method to fetch oil prices in USD from the API
    public static Map<String, String> fetchOilPriceInUSD() throws APIStatusException {
        try {
            // Send HTTP request to the API
            HttpResponse<String> response = sendHttpRequest();

            // Check if the HTTP response status code is 200 (OK)
            if (response.statusCode() == 200) {
                // Parse and extract oil prices from the response
                System.out.println("Oil prices API received succesfully!");
                return parseAndExtractOilPrice(response.body());
            } else {
                // Throw an exception for non-OK status codes
                throw new APIStatusException("Failed to fetch oil prices. HTTP Status Code: " + response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new APIStatusException("Failed to fetch oil prices due to an IO or InterruptedException.", e);
        }
    }

    // Method to send HTTP request to the API
    private static HttpResponse<String> sendHttpRequest() throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + API_TOKEN + API_PARAMETERS))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // Method to parse and extract oil prices from JSON response
    private static Map<String, String> parseAndExtractOilPrice(String responseBody) {
        Map<String, String> result = new LinkedHashMap<>();

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
                        String value = dataEntry.get("value").getAsString();

                        // Put the extracted information into the result map
                        result.put(period, value);
                    }
                } else {
                    // Handle the case where "data" is a JsonObject
                    System.out.println("Unexpected JSON structure: 'data' is not an array.");
                    throw new RuntimeException("Unexpected JSON structure: 'data' is not an array.");
                }
            } else {
                // Handle the case where the key "data" is not present in the JSON response
                System.out.println("JSON response does not contain the 'data' key.");
                throw new RuntimeException("Unexpected JSON structure: 'data' is not an array.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
