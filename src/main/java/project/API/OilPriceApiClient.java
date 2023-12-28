package project.API;

import project.exceptions.APIStatusException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class OilPriceApiClient {
    private static final LocalDate StartingDate = LocalDate.of(2023, 4, 1);
    private static final String API_URL = "https://api.eia.gov/v2/petroleum/pri/spt/data/";
    private static final String API_TOKEN = "?api_key=1hU4hrUQ8qs1uR4L9UScdgCAqhDLNRBAmg9cchbv";
    private static final String API_PARAMETERS = "&frequency=daily&data[0]=value&facets[series][]=RBRTE&start=" + StartingDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + "&sort[0][column]=period&sort[0][direction]=desc&offset=0&length=5000";
    private static final Double bblToLiters = 158.987295;
    private static final Logger logger = Logger.getLogger(OilPriceApiClient.class.getName());

    static {
        // Configure the logger to write log messages to a file in the "Logs" folder
        try {
            // Create the "Logs" folder if it doesn't exist
            File logsFolder = new File("Logs");
            if (!logsFolder.exists()) {
                logsFolder.mkdirs();
            }

            // Create a file handler with a name containing class name, date, and time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String logFileName = String.format("Logs/%s_%s.log", OilPriceApiClient.class.getSimpleName(), dateFormat.format(new Date()));
            FileHandler fileHandler = new FileHandler(logFileName);

            // Set formatter and add the file handler to the logger
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to fetch oil prices in USD from the API
    public static Map<String, List<String>> fetchOilPriceInUSD() throws APIStatusException {
        try {
            // Send HTTP request to the API
            HttpResponse<String> response = sendHttpRequest();
            Map<String, List<String>> result = new LinkedHashMap<>();
            // Check if the HTTP response status code is 200 (OK)
            if (response.statusCode() == 200) {
                // Parse and extract oil prices from the response
                result = parseAndExtractOilPrice(response.body());
                logger.info("Oil prices API received successfully!");
                return result;
            } else {
                // Throw an exception for non-OK status codes
                throw new APIStatusException("Failed to fetch oil prices. HTTP Status Code: " + response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            logger.log(Level.SEVERE, "Failed to fetch oil prices due to IO or InterruptedException.", e);
            throw new APIStatusException("Failed to fetch oil prices due to IO or InterruptedException.", e);
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
    private static Map<String, List<String>> parseAndExtractOilPrice(String responseBody) {
        Map<String, List<String>> result = new LinkedHashMap<>();

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
                        List<String> value = new ArrayList<>();
                        // Give a value of one bbl in USD
                        value.add(dataEntry.get("value").getAsString());
                        // Give a value of one liter in USD
                        Double valueInLiters = dataEntry.get("value").getAsDouble() / bblToLiters;
                        String formattedValue = String.format("%.2f", valueInLiters);
                        value.add(formattedValue.replace(",", "."));

                        result.put(period, value);
                    }
                } else {
                    // Handle the case where "data" is a JsonObject
                    logger.log(Level.SEVERE, "Unexpected JSON structure: 'data' is not an array.");
                    throw new RuntimeException("Unexpected JSON structure: 'data' is not an array.");
                }
            } else {
                // Handle the case where the key "data" is not present in the JSON response
                logger.log(Level.SEVERE, "JSON response does not contain the 'data' key.");
                throw new RuntimeException("Unexpected JSON structure: 'data' is not an array.");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing JSON response.", e);
        }

        // Fill in missing dates with the average between the nearest past and future dates
        result = fillMissingDates(result);

        return result;
    }

    // Method to fill in missing dates with the average between the nearest past and future dates
    private static Map<String, List<String>> fillMissingDates(Map<String, List<String>> oilPrices) {
        Map<String, List<String>> result = new TreeMap<>(oilPrices);
        List<String> missingDates = new ArrayList<>();
        // Get all dates
        List<String> allDates = new LinkedList<>();
        LocalDate missingDate = StartingDate;

        while (!missingDate.isAfter(LocalDate.now())) {
            allDates.add(missingDate.toString());
            missingDate = missingDate.plusDays(1);
        }

        // Iterate through all dates to find missing ones
        for (int i = 0; i < allDates.size(); i++) {
            String currentDate = allDates.get(i);

            // Check if there are values for the current date
            if (!result.containsKey(currentDate)) {
                // Add the missing date to the list
                missingDates.add(currentDate);
            }
        }

        // Iterate through all dates
        for (int i = 0; i < allDates.size(); i++) {
            String currentDate = allDates.get(i);

            // Check if there are values for the current date
            if (!result.containsKey(currentDate)) {
                // Find the nearest past date
                String previousDate = findPreviousDate(allDates, i);

                // Find the nearest future date
                String nextDate = findNextDate(allDates, i);

                // Calculate the average between the nearest past and future dates
                double averageValue = (Double.parseDouble(result.get(previousDate).get(0)) +
                        Double.parseDouble(result.get(nextDate).get(0))) / 2;

                // Add the average to the result map
                List<String> averageValues = new ArrayList<>();
                averageValues.add(String.valueOf(averageValue));
                result.put(currentDate, averageValues);
            }
        }

        return result;
    }

    // Method to find the nearest past date
    private static String findPreviousDate(List<String> dates, int currentIndex) {
        for (int i = currentIndex - 1; i >= 0; i--) {
            if (dates.get(i) != null) {
                return dates.get(i);
            }
        }
        return dates.get(0); // Return the first date if no previous date is found
    }

    // Method to find the nearest future date
    private static String findNextDate(List<String> dates, int currentIndex) {
        for (int i = currentIndex + 1; i < dates.size(); i++) {
            if (dates.get(i) != null) {
                return dates.get(i);
            }
        }
        return dates.get(dates.size() - 1); // Return the last date if no next date is found
    }
}
