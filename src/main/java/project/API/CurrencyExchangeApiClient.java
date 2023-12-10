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
    private static LocalDate APIDate = LocalDate.of(1999, 2, 1);
    private static LocalDate currentDate = LocalDate.now();
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

    public static Map<String, String> fetchExchangeRate() throws APIStatusException {
        HttpResponse<String> response;
        Map<String, String> exchangeRate = new HashMap<>();
        try {
            while (APIDate.isBefore(currentDate) || APIDate.isEqual(currentDate)) {
                response = sendHttpRequest();
                if (response.statusCode() == 200) {
                    String formattedDate = APIDate.format(formatter);
                    exchangeRate.put(formattedDate, parseAndExtractExchangeRate(response.body()));
                } else
                    throw new APIStatusException(
                            "Failed to fetch exchange prices. HTTP Status Code: " + response.statusCode());
                APIDate = APIDate.plusMonths(1);
            }
            return exchangeRate;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new APIStatusException("Failed to fetch oil prices due to an IO or InterruptedException.", e);
        }
    }

    private static HttpResponse<String> sendHttpRequest()
            throws IOException, InterruptedException {
        String urlWithParams = String.format("%s%s?access_key=%s&symbols=%s",
                API_URL, APIDate, API_ACCESS_KEY, targetCurrency);

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlWithParams))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String parseAndExtractExchangeRate(String responseBody) {
        String result = new String();
        try {
            // Parse the JSON response
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

            double usdValue = jsonResponse.getAsJsonObject("rates").get("USD").getAsDouble();
            double plnValue = jsonResponse.getAsJsonObject("rates").get("PLN").getAsDouble();

            result = String.format("%.5f %.5f", usdValue, plnValue);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

}
