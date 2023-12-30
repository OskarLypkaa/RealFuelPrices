package project.API;

import project.exceptions.APIStatusException;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;



public class CurrencyExchangeApiClient extends ApiClient{
    private static final String API_URL = "http://api.nbp.pl/api/exchangerates/rates/a/";
    private static final LocalDate currentDate = LocalDate.now();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    @Override
    public Map<String, List<String>> fetchData() throws APIStatusException {
        getLogger().info("Fetching exchange rates from the API...");

        LocalDate APIDate = LocalDate.of(2022, 4, 1);
        HttpResponse<String> response;
        Map<String, List<String>> exchangeRate = new LinkedHashMap<>();

        try {
            // Iterate through dates from API start date to current date
            while (APIDate.getYear() <= currentDate.getYear()) {
                logger.info("Working on: " + APIDate.getYear());
                // Time sleep for API not to receive status code: 429
                Thread.sleep(100);

                // Send HTTP request to the API for EUR
                response = sendHttpRequest(API_URL ,"" , generateApiParams("eur", APIDate));

                // Check if the HTTP response status code is 200 (OK)
                if (response.statusCode() == 200) {
                    // Format date and parse exchange rate from response for EUR
                    exchangeRate.putAll(fetchOneYearExchangeRates(response.body(), APIDate));
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
    private static Map<String, List<String>> fetchOneYearExchangeRates(String responseBody, LocalDate startingDate) {
        Map<String, String> oneYearExchangeRateMap = parseAndExtractExchangeRate(responseBody);
        Map<String, List<String>> formatedOneYearExchangeRateMap = new LinkedHashMap<>();

        LocalDate newAPIDate = startingDate;

        while (!newAPIDate.isEqual(startingDate.plusYears(1).minusDays(1)) && newAPIDate.isBefore(currentDate)) {
            List<String> tempList = new LinkedList<>();
            if (oneYearExchangeRateMap.get(newAPIDate.toString()) != null)
            {
                tempList.add(oneYearExchangeRateMap.get(newAPIDate.toString()));
                formatedOneYearExchangeRateMap.put(newAPIDate.toString(), tempList);
            }
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
                
                tempList.add(formatedAvrValue);
                formatedOneYearExchangeRateMap.put(newAPIDate.toString(), tempList);
            }

            newAPIDate = newAPIDate.plusDays(1);
        }

        return formatedOneYearExchangeRateMap;
    }



    // Method to generate API parameters
    private static String generateApiParams(String currencyCode, LocalDate startingDate) throws IOException, InterruptedException {
        String apiParams;
        // Create the API URL with parameters
        if (startingDate.getYear() != currentDate.getYear()) {
            String nextYearDate = startingDate.plusYears(1).format(formatter);
            apiParams = String.format("%s/%s/%s/?format=json", currencyCode,startingDate.format(formatter), nextYearDate);
        } else {
            apiParams = String.format("%s/%s/%s/?format=json", currencyCode, startingDate.format(formatter), currentDate);
        }
        return apiParams;
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
