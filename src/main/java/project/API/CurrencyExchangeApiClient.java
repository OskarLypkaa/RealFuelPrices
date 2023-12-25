package project.API;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import project.exceptions.APIStatusException;

public class CurrencyExchangeApiClient {
    private static final String API_URL = "http://api.nbp.pl/api/exchangerates/rates/a/";
    private static final Logger logger = Logger.getLogger(CurrencyExchangeApiClient.class.getName());

    static {
        // Configure the logger to write log messages to a file
        try {
            FileHandler fileHandler = new FileHandler("currency_exchange_api.log");
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static LocalDate currentDate = LocalDate.now();
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Method to fetch exchange rates for EUR and USD from the API
    public static Map<String, List<String>> fetchExchangeRate() throws APIStatusException {
        logger.info("Fetching exchange rates from the API...");

        LocalDate APIDate = LocalDate.of(2022, 4, 1);
        HttpResponse<String> response;
        Map<String, List<String>> exchangeRate = new LinkedHashMap<>();
        List<String> oneYearExchangeRateList;

        try {
            // Iterate through dates from API start date to current date
            while (APIDate.getYear() <= currentDate.getYear()) {
                logger.info("Working on: " + APIDate.getYear());
                // Time sleep for API not to receive status code: 429
                Thread.sleep(100);

                // Send HTTP request to the API for EUR
                response = sendHttpRequest("eur", APIDate);

                // Check if the HTTP response status code is 200 (OK)
                if (response.statusCode() == 200) {
                    // Format date and parse exchange rate from response for EUR
                    oneYearExchangeRateList = fetchOneYearExchangeRates(response.body(), APIDate);
                    exchangeRate.put(APIDate.toString(), oneYearExchangeRateList);
                } else {
                    // Throw an exception for non-OK status codes
                    throw new APIStatusException(
                            "Failed to fetch exchange prices. HTTP Status Code: " + response.statusCode());
                }
                logger.info("Request successful for year: " + APIDate.getYear());
                APIDate = APIDate.plusYears(1);
            }
        } catch (IOException | InterruptedException e) {
            logger.log(Level.SEVERE, "Failed to fetch exchanged rates due to IO or InterruptedException.", e);
            throw new APIStatusException("Failed to fetch exchanged rates due to IO or InterruptedException.", e);
        }
        logger.info("Exchange prices API received successfully!");
        return exchangeRate;
    }


    // Method to fetch exchange rates for one year and return a list of values
    private static List<String> fetchOneYearExchangeRates(String responseBody, LocalDate startingDate) {
        Map<String, String> oneYearExchangeRateMap = parseAndExtractExchangeRate(responseBody);
        List<String> oneYearExchangeRateList = new LinkedList<>();

        LocalDate newAPIDate = startingDate;

        while (!newAPIDate.isEqual(startingDate.plusYears(1).minusDays(1)) && newAPIDate.isBefore(currentDate)) {
            if (oneYearExchangeRateMap.get(newAPIDate.toString()) != null)
                oneYearExchangeRateList.add(oneYearExchangeRateMap.get(newAPIDate.toString()));
            else {
                LocalDate decreasingAPIDate = newAPIDate.minusDays(1);
                LocalDate increasingAPIDate = newAPIDate.plusDays(1);
                String valueBefore = oneYearExchangeRateMap.get(decreasingAPIDate.toString());
                String valueAfter = oneYearExchangeRateMap.get(increasingAPIDate.toString());


                
                while (valueBefore != null) {
                    if (oneYearExchangeRateMap.containsKey(decreasingAPIDate.toString())) {
                        valueBefore = oneYearExchangeRateMap.get(decreasingAPIDate.toString());
                        break;
                    } else {
                        decreasingAPIDate = decreasingAPIDate.minusDays(1);
                    }
                }
                

                while (!increasingAPIDate.isEqual(startingDate.plusYears(1).minusDays(1)) && increasingAPIDate.isBefore(currentDate)) {
                    if (oneYearExchangeRateMap.containsKey(increasingAPIDate.toString())) {
                        valueAfter = oneYearExchangeRateMap.get(increasingAPIDate.toString());
                        break;
                    } else {
                        increasingAPIDate = increasingAPIDate.plusDays(1);
                    }
                }

                
                Double avrValue;
                String formatedAvrValue = new String();
                if (newAPIDate.getDayOfMonth() == 1 || valueAfter == null) {
                    if (valueAfter != null) {
                        avrValue = Double.parseDouble(valueAfter);
                        valueBefore = valueAfter;
                    } else {
                        avrValue = Double.parseDouble(valueBefore);
                    }
                } else {
                    avrValue = (Double.parseDouble(valueBefore) + (valueAfter != null ? Double.parseDouble(valueAfter) : 0)) / 2;
                }
                
                DecimalFormat decimalFormat = new DecimalFormat("#.####");
                formatedAvrValue = decimalFormat.format(avrValue); 

                oneYearExchangeRateMap.put(newAPIDate.toString(), valueBefore);

                logger.info("avrValue for date " + newAPIDate + ": " + formatedAvrValue);
                oneYearExchangeRateList.add(formatedAvrValue);
            }

            newAPIDate = newAPIDate.plusDays(1);
        }

        return oneYearExchangeRateList;
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
        Map<String, String> result = new LinkedHashMap<>();
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
            logger.log(Level.SEVERE, "Error parsing JSON response.", e);
        }
        return result;
    }
}
