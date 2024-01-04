package project.analysis;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import project.API.ApiClient;
import project.API.Clients.CurrencyExchangeApiClient;
import project.API.Clients.OilPriceApiClient;
import project.Selenium.WebScraper;
import project.Selenium.Screpers.NumbeoWebScraper;
import project.Selenium.Screpers.OrlenWebScraper;
import project.exceptions.APIStatusException;
import project.exceptions.WSDataException;

public class DataRunner {

        // Values for data fetching
        private static final String EURO = "eur";
        private static final String USD = "usd";
        private static final String GAS_95 = "Benzyna bezołowiowa - Eurosuper 95";
        private static final String GAS_98 = "Benzyna bezołowiowa - Super Plus 98";
        private static final String DISEL = "Olej Napędowy Ekodiesel";
        private static final String AVERAGE_INCOME = "https://www.numbeo.com/cost-of-living/prices_by_country.jsp?displayCurrency=USD&itemId=105";
        private static final String FUEL_PRICE = "https://www.numbeo.com/cost-of-living/prices_by_country.jsp?displayCurrency=USD&itemId=24";

        // Create instances of API clients
        private static final ApiClient currencyApi = new CurrencyExchangeApiClient();
        private static final ApiClient oilApi = new OilPriceApiClient();
        private static final WebScraper orlenWebScraper = new OrlenWebScraper();
        private static final WebScraper numbeoWebScraper = new NumbeoWebScraper();

        public static void main(String[] args) {
                // Initialize asynchronous tasks
                CompletableFuture<Map<String, List<String>>> task1 = CompletableFuture
                                .supplyAsync(() -> executeTask(() -> fetchDataWithCurrency(EURO), "Task 1"));
                CompletableFuture<Map<String, List<String>>> task2 = CompletableFuture
                                .supplyAsync(() -> executeTask(() -> fetchDataWithCurrency(USD), "Task 2"));
                CompletableFuture<Map<String, List<String>>> task3 = CompletableFuture
                                .supplyAsync(() -> executeTask(() -> fetchDataForOil(), "Task 3"));
                CompletableFuture<Map<String, List<String>>> task4 = CompletableFuture
                                .supplyAsync(() -> executeTask(() -> fetchDataForOrlenPrices(GAS_95), "Task 4"));
                CompletableFuture<Map<String, List<String>>> task5 = CompletableFuture
                                .supplyAsync(() -> executeTask(() -> fetchDataForOrlenPrices(GAS_98), "Task 5"));
                CompletableFuture<Map<String, List<String>>> task6 = CompletableFuture
                                .supplyAsync(() -> executeTask(() -> fetchDataForOrlenPrices(DISEL), "Task 6"));
                CompletableFuture<Map<String, List<String>>> task7 = CompletableFuture
                                .supplyAsync(() -> executeTask(() -> fetchDataForCurrentPrices(AVERAGE_INCOME),
                                                "Task 7"));
                CompletableFuture<Map<String, List<String>>> task8 = CompletableFuture
                                .supplyAsync(() -> executeTask(() -> fetchDataForCurrentPrices(FUEL_PRICE), "Task 8"));

                List<CompletableFuture<Map<String, List<String>>>> allTasks = List.of(
                                task1, task2, task3, task4, task5, task6, task7, task8);

                CompletableFuture<Void> allOf = CompletableFuture
                                .allOf(allTasks.toArray(new CompletableFuture[0]));

                // Wait for completion of all tasks
                allOf.join();

                // Print information about the results of each task
                for (int i = 0; i < allTasks.size(); i++) {
                        CompletableFuture<Map<String, List<String>>> task = allTasks.get(i);
                        try {
                                Map<String, List<String>> result = task.get();
                                System.out.println("Task " + (i + 1) + " Done");
                        } catch (InterruptedException | ExecutionException e) {
                                // Print error messages for each failed task
                                e.printStackTrace();

                                // Identify which task failed
                                if (e.getCause() instanceof APIStatusException) {
                                        System.out.println("Task " + (i + 1) + " Failed: APIStatusException");
                                } else if (e.getCause() instanceof WSDataException) {
                                        System.out.println("Task " + (i + 1) + " Failed: WSDataException");
                                } else {
                                        System.out.println("Task " + (i + 1) + " Failed: Unknown error occurred.");
                                }
                        }
                }

                System.out.println("All tasks completed.");
        }

        private static Map<String, List<String>> fetchDataWithCurrency(String currency) throws APIStatusException {
                return currencyApi.fetchData(currency);
        }

        private static Map<String, List<String>> fetchDataForOil() throws APIStatusException {
                return oilApi.fetchData();
        }

        private static Map<String, List<String>> fetchDataForOrlenPrices(String fuelType) throws WSDataException {
                return orlenWebScraper.fetchData(fuelType);
        }

        private static Map<String, List<String>> fetchDataForCurrentPrices(String dataSource) throws WSDataException {
                return numbeoWebScraper.fetchData(dataSource);
        }

        private static <T> T executeTask(TaskSupplier<T> taskSupplier, String taskName) {
                try {
                        return taskSupplier.get();
                } catch (Exception e) {
                        System.out.println(taskName + " Failed: " + e.getClass().getSimpleName());
                        return null;
                }
        }

        interface TaskSupplier<T> {
                T get() throws Exception;
        }
}
