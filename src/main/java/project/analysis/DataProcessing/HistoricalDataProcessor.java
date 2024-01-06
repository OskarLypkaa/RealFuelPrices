package project.analysis.DataProcessing;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HistoricalDataProcessor extends DataProcessor {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Map<String, List<String>> processHistoricalData(List<Map<String, List<String>>> historicalResultList) {
        List<Map<String, List<String>>> correctedHistoricalResultList = new LinkedList<>();

        for (Map<String, List<String>> individualMap : historicalResultList) {
            Map<String, List<String>> filledMap = fillMapWithMissingDates(individualMap);
            filledMap = sortMapByDate(filledMap);
            filledMap = fillMapWithMissingData(filledMap);
            correctedHistoricalResultList.add(filledMap);
        }

        Map<String, List<String>> resultMap = mergeMaps(correctedHistoricalResultList);
        return resultMap;
    }

    public Map<String, List<String>> processCurrentData(List<Map<String, List<String>>> historicalResultList) {
        return null;
    }

    private Map<String, List<String>> fillMapWithMissingDates(Map<String, List<String>> dataMap) {
        Map<String, List<String>> result = new LinkedHashMap<>(dataMap);

        LocalDate currentDate = LocalDate.now();
        LocalDate cutoffDate = LocalDate.parse("2004-04-01");

        while (!cutoffDate.isAfter(currentDate)) {
            String formattedDate = cutoffDate.format(DATE_FORMATTER);

            result.putIfAbsent(formattedDate, new ArrayList<>());

            cutoffDate = cutoffDate.plusDays(1);
        }

        return result;
    }

    public Map<String, List<String>> sortMapByDate(Map<String, List<String>> dataMap) {
        System.out.println("Sorting...");
        return dataMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
    }

    // Fills missing data in the given dataMap by calculating averages of nearby
    // values.
    // If data is missing at the edges, copies the nearest available value.
    public Map<String, List<String>> fillMapWithMissingData(Map<String, List<String>> dataMap) {
        Map<String, List<String>> result = new LinkedHashMap<>(dataMap);

        for (Map.Entry<String, List<String>> entry : result.entrySet()) {
            String date = entry.getKey();
            List<String> values = entry.getValue();

            if (values.isEmpty()) {
                int i = 0;
                do {
                    try {
                        // If a value is missing, fill it with the calculated average
                        double average = calculateAverage(result, date, i);
                        values.add(i, String.valueOf(average));
                        i++;
                    } catch (Exception e) {
                        break;
                    }
                } while (i != 10);
            }
        }
        return result;
    }

    // Calculates the average of nearby values for the specified date and index.
    private double calculateAverage(Map<String, List<String>> dataMap, String date, int index) {
        List<Double> nearbyValues = new ArrayList<>();

        // Search for values in the past
        LocalDate currentDate = LocalDate.parse(date, DATE_FORMATTER);
        boolean firstIteration = true;

        while (true) {
            if (!firstIteration) {
                currentDate = currentDate.minusDays(1);
                if (!currentDate.isBefore(LocalDate.of(2004, 4, 1))) {
                    List<String> value = dataMap.get(currentDate.format(DATE_FORMATTER));
                    if (!value.isEmpty()) {
                        nearbyValues.add(Double.parseDouble(value.get(index)));
                        break;
                    }
                } else
                    break;

            } else
                firstIteration = false;
        }

        currentDate = LocalDate.parse(date, DATE_FORMATTER);
        firstIteration = true;
        // Search for values in the future
        while (true) {
            if (!firstIteration) {
                currentDate = currentDate.plusDays(1);
                if (!currentDate.isAfter(LocalDate.now())) {
                    List<String> value = dataMap.get(currentDate.format(DATE_FORMATTER));
                    if (!value.isEmpty()) {
                        nearbyValues.add(Double.parseDouble(value.get(index)));
                        break;
                    }
                } else
                    break;

            } else
                firstIteration = false;

        }

        // If data is missing at the edges, copy the last nearest value
        if (nearbyValues.size() > 1) {
            // Calculate the average
            double sum = 0;
            for (Double value : nearbyValues) {
                sum += value;
            }
            return sum / nearbyValues.size();
        } else {
            return nearbyValues.get(0);
        }
    }
}
