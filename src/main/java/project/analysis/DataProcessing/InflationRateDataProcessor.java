package project.analysis.DataProcessing;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class InflationRateDataProcessor extends DataProcessor {

    public Map<String, List<String>> processInflationData(List<Map<String, List<String>>> inflationResultList) {

        inflationResultList.add(getMonthAveragePrices(inflationResultList.get(0)));

        inflationResultList.remove(0);

        Map<String, List<String>> resultMap = mergeMaps(inflationResultList);

        return resultMap;
    }

    private static Map<String, List<String>> getMonthAveragePrices(Map<String, List<String>> fuelPricesList) {
        Map<String, List<String>> result = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> entry : fuelPricesList.entrySet()) {
            String date = entry.getKey();
            List<String> prices = entry.getValue();

            try {
                Date currentDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
                String monthKey = new SimpleDateFormat("yyyy-MM").format(currentDate);

                if (!result.containsKey(monthKey)) {
                    result.put(monthKey, new ArrayList<>());
                }

                result.get(monthKey).addAll(prices);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // Oblicz średnią dla każdego miesiąca
        Map<String, List<String>> averageResult = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : result.entrySet()) {
            String month = entry.getKey();
            List<String> prices = entry.getValue();
            double sum = prices.stream().mapToDouble(Double::parseDouble).sum();
            double average = sum / prices.size();
            averageResult.put(month, Arrays.asList(String.format("%.2f", average).replace(',', '.')));

        }

        return averageResult;
    }
}
