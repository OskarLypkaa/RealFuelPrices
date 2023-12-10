package project.analysis;

import java.util.LinkedHashMap;
import java.util.Map;

public class OilPriceAnalyzer {
    private Map<String, String> oilPricesInUSD;
    private Map<String, String> exchangeRates;

    public OilPriceAnalyzer(Map<String, String> oilPricesInUSD, Map<String, String> exchangeRates) {
        this.oilPricesInUSD = oilPricesInUSD;
        this.exchangeRates = exchangeRates;
    }

    public Map<String, String> analyzedOilPricesInPLN() {
        Map<String, String> analyzedResults = new LinkedHashMap<>();

        for (String date : oilPricesInUSD.keySet()) {
            double oilPriceInUSD = Double.parseDouble(oilPricesInUSD.get(date));
            String exchangeRateInfo = exchangeRates.get(date);

            double exchangeRatePLN = extractExchangeRatePLN(exchangeRateInfo);
            double oilPriceInPLN = oilPriceInUSD * exchangeRatePLN;

            analyzedResults.put(date, String.format("%.2f", oilPriceInPLN));
        }
        return analyzedResults;
    }

    private Double extractExchangeRatePLN(String exchangeRateInfo) {
        String[] parts = exchangeRateInfo.split(" ");
        if (parts.length == 2) {
            try {
                Double USD = Double.parseDouble(parts[0].replace(",", "."));
                Double PLN = Double.parseDouble(parts[1].replace(",", "."));
                return PLN / USD;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 1.0;
    }
}
