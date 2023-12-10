package project.analysis;

import java.util.LinkedHashMap;
import java.util.Map;

public class OilPriceAnalyzer {
    private Map<String, String> oilPricesInUSD;
    private Map<String, String> exchangeRates;

    // Constructor to initialize the class with oil prices in USD and exchange rates
    public OilPriceAnalyzer(Map<String, String> oilPricesInUSD, Map<String, String> exchangeRates) {
        this.oilPricesInUSD = oilPricesInUSD;
        this.exchangeRates = exchangeRates;

    }

    // Analyze oil prices in PLN based on USD prices and exchange rates
    public Map<String, String> analyzedOilPricesInPLN() {
        Map<String, String> analyzedResults = new LinkedHashMap<>();

        // Iterate through each date in the oilPricesInUSD map
        for (String date : oilPricesInUSD.keySet()) {
            // Extract oil price in USD and corresponding exchange rate info
            double oilPriceInUSD = Double.parseDouble(oilPricesInUSD.get(date));
            String exchangeRateInfo = exchangeRates.get(date);

            // Calculate oil price in PLN based on the exchange rate
            double exchangeRatePLN = extractExchangeRatePLN(exchangeRateInfo);
            double oilPriceInPLN = oilPriceInUSD * exchangeRatePLN;

            // Format the result and add it to the analyzedResults map
            analyzedResults.put(date, String.format("%.2f", oilPriceInPLN));
        }
        return analyzedResults;
    }

    // Extracts the exchange rate in PLN from the given exchange rate information
    private double extractExchangeRatePLN(String exchangeRateInfo) {
        String[] parts = exchangeRateInfo.split(" ");
        if (parts.length == 2) {
            try {
                // Parse USD and PLN values and calculate the exchange rate
                double USD = Double.parseDouble(parts[0].replace(",", "."));
                double PLN = Double.parseDouble(parts[1].replace(",", "."));
                return PLN / USD;
            } catch (NumberFormatException e) {
                e.printStackTrace(); // Consider logging the exception instead of printing the stack trace.
            }
        }
        return 1.0; // Default to 1.0 if the format is incorrect
    }

    public void printData(Map<String, String> fuelPricesInPLN) {
        Map<String, String> analiseResult = analyzedOilPricesInPLN();

        try {
            for (String date : analiseResult.keySet()) {
                String valueString = fuelPricesInPLN.get(date);
                Double value = Double.valueOf(valueString);
                value = value / 1000;
                System.out.println("Wartość bartyłki ropy w " + date + ": " +
                        analiseResult.get(date) + "PLN  " + String.format("%.2f", value) + "PLN/L");
                Thread.sleep(500);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
