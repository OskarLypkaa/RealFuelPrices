package project.analysis;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class DataProcessor {

    public CompletableFuture<Map<String, List<String>>> processDataAsync(
            Map<String, List<String>> orlenData,
            Map<String, List<String>> numbeoData) {

        // Sample asynchronous task 1
        CompletableFuture<Map<String, List<String>>> task1 = CompletableFuture
                .supplyAsync(() -> processOrlenData(orlenData));

        // Sample asynchronous task 2
        CompletableFuture<Map<String, List<String>>> task2 = CompletableFuture
                .supplyAsync(() -> processNumbeoData(numbeoData));

        // When both tasks are completed, perform an additional operation
        CompletableFuture<Void> combinedTask = CompletableFuture.allOf(task1, task2);

        // Use thenAcceptBothAsync to combine the results of both tasks and process the
        // data
        return combinedTask.thenApplyAsync(ignoredResult -> {
            Map<String, List<String>> processedOrlenData = task1.join();
            Map<String, List<String>> processedNumbeoData = task2.join();

            // Perform additional analysis and transformation logic if needed

            // Merge and process the combined data
            Map<String, List<String>> mergedData = mergeAndProcessData(
                    processedOrlenData, processedNumbeoData);

            return mergedData;
        });
    }

    private Map<String, List<String>> processOrlenData(Map<String, List<String>> orlenData) {
        // Perform processing logic for Orlen data
        // For example, you can transform the data or perform specific analysis
        return orlenData;
    }

    private Map<String, List<String>> processNumbeoData(Map<String, List<String>> numbeoData) {
        // Perform processing logic for Numbeo data
        // For example, you can transform the data or perform specific analysis
        return numbeoData;
    }

    private Map<String, List<String>> mergeAndProcessData(
            Map<String, List<String>> processedOrlenData,
            Map<String, List<String>> processedNumbeoData) {

        // Merge and process the data
        List<Map<String, List<String>>> dataList = List.of(processedOrlenData, processedNumbeoData);

        Map<String, List<String>> mergedData = mergeMaps(dataList);
        analyzeAndTransformData(mergedData);

        return mergedData;
    }

    private void analyzeAndTransformData(Map<String, List<String>> data) {
        // Perform analysis and transformation logic here
        System.out.println("Analysis and transformation of data:");
        for (Map.Entry<String, List<String>> entry : data.entrySet()) {
            System.out.print(entry.getKey());
            List<String> values = entry.getValue();
            for (String value : values)
                System.out.print(" " + value);

            System.out.println();
            System.out.println("---------------");
        }
    }

    public static <K, V> Map<K, List<V>> mergeMaps(List<Map<K, List<V>>> maps) {
        Map<K, List<V>> mergedMap = maps.stream()
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (list1, list2) -> {
                            List<V> mergedList = new ArrayList<>(list1);
                            mergedList.addAll(list2);
                            return mergedList;
                        }));

        return mergedMap;
    }
}
