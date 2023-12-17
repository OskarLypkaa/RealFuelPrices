package project.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CurrentDataAnalyzer {
    public static void main(String[] args) {
        // Sample asynchronous task 1
        CompletableFuture<String> task1 = CompletableFuture.supplyAsync(() -> fetchDataFromSource1());

        // Sample asynchronous task 2
        CompletableFuture<String> task2 = CompletableFuture.supplyAsync(() -> fetchDataFromSource2());

        // When both tasks are completed, perform an additional operation
        CompletableFuture<Void> combinedTask = CompletableFuture.allOf(task1, task2);
        combinedTask.thenRun(() -> {
            try {
                // Get results from both tasks
                String result1 = task1.get();
                String result2 = task2.get();

                // Perform additional operation with the results
                analyzeAndTransformData(result1, result2);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

        // Wait until all tasks are completed
        combinedTask.join();
    }

    private static String fetchDataFromSource1() {
        // Fetch data from the first source (could be asynchronous)
        return "Data from source 1";
    }

    private static String fetchDataFromSource2() {
        // Fetch data from the second source (could be asynchronous)
        return "Data from source 2";
    }

    private static void analyzeAndTransformData(String result1, String result2) {
        // Analyze and transform the data
        System.out.println("Analysis and transformation of data:");
        System.out.println("Original Result 1: " + result1);
        System.out.println("Original Result 2: " + result2);

        // Perform analysis and transformation logic here
        // For example, concatenate the results
        String transformedData = result1 + " | " + result2;

        System.out.println("Transformed Data: " + transformedData);
    }

    public void printData(Map<String, List<String>> data) {
        System.out.println("Printing Data:");
        for (Map.Entry<String, List<String>> entry : data.entrySet()) {
            System.out.print(entry.getKey());
            List<String> values = entry.getValue();
            for(String value : values)
                System.out.print(" " + value);
            
        System.out.println();
        System.out.println("---------------");
        }
    }

    public static <K, V> Map<K, List<V>> mergeMaps(List<Map<K, List<V>>> maps) {
        Map<K, List<V>> mergedMap = new HashMap<>();
    
        for (Map<K, List<V>> map : maps) {
            for (Map.Entry<K, List<V>> entry : map.entrySet()) {
                K key = entry.getKey();
                List<V> value = entry.getValue();

                mergedMap.merge(key, value, (list1, list2) -> {
                    List<V> mergedList = new ArrayList<>(list1);
                    mergedList.addAll(list2);
                    return mergedList;
                });
            }
        }
    
        return mergedMap;
    }
}
