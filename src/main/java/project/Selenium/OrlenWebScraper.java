package project.Selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import project.exceptions.WSDataException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class OrlenWebScraper extends WebScraper {

    @Override
    public Map<String, List<String>> fetchData() throws WSDataException {

        // Initialize Chrome browser
        WebDriver driver = initializeChromeDriver();

        try {

            // Open the website
            driver.get("https://www.orlen.pl/pl/dla-biznesu/hurtowe-ceny-paliw#paliwa-archive");

            // Wait for the "I accept" (cookie consent) button to appear and click it
            WebDriverWait wait = new WebDriverWait(driver, 10);
            WebElement acceptButton = wait.until(ExpectedConditions
                    .elementToBeClickable(By.id("CybotCookiebotDialogBodyLevelButtonLevelOptinAllowAll")));

            if (acceptButton.isDisplayed()) {
                acceptButton.click();
            }

            // Get data from the table with a specified class
            Map<String, List<String>> tableData = new LinkedHashMap<>();

            // Iterate through years, select each year, and scrape data
            for (int year = LocalDate.now().getYear(); year >= 2004; year--) {
                selectYear(driver, year);
                tableData.putAll(scrapeTableDataForFirstDays(driver, "table--effectivedate"));
            }

            // Check if data is available for all months in each year
            Set<String> missingDataMonths = findMissingDataMonths(tableData);

            if (!missingDataMonths.isEmpty()) {
                System.out.println("Missing data for months and years:");
                for (String missingMonth : missingDataMonths) {
                    System.out.println(missingMonth);
                }
                throw new WSDataException("Missing data for Orlen web scraper");
            }

            System.out.println("Fuel prices received successfully!");
            return tableData;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("InterruptedException: " + e.getMessage());
            throw new WSDataException("Failed to fetch fuel prices due to InterruptedException.", e);
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            throw new WSDataException("Failed to fetch fuel prices.", e);
        } finally {
            // Close the browser
            if (driver != null) {
                driver.quit();
            }
        }
    }

    // Function to find missing data months
    private Set<String> findMissingDataMonths(Map<String, List<String>> tableData) {
        Set<String> monthsWithData = tableData.keySet();
        Set<String> allMonthsInAllYears = generateAllMonthsInAllYearsSet();
        Set<String> missingDataMonths = new HashSet<>(allMonthsInAllYears);

        missingDataMonths.removeAll(monthsWithData);

        return missingDataMonths;
    }

    // Function to generate all months in all years
    private Set<String> generateAllMonthsInAllYearsSet() {
        Set<String> allMonthsInAllYears = new HashSet<>();
        int currentYear = LocalDate.now().getYear();

        for (int year = 2004; year <= currentYear; year++) {
            for (int month = 1; month <= 12; month++) {
                allMonthsInAllYears.add(formatToYearMonth("01-" + String.format("%02d", month) + "-" + year));
            }
        }
        return allMonthsInAllYears;
    }

    // Function to select a year from the dropdown
    private void selectYear(WebDriver driver, int year) {
        WebDriverWait wait = new WebDriverWait(driver, 10);

        // Find the dropdown element
        List<WebElement> dropdowns = driver.findElements(By.className("choices"));

        // Click on the dropdown to expand the options
        dropdowns.get(1).click();

        // Find the option with the specified year based on the data-value attribute
        By yearOptionSelector = By.cssSelector(".choices__list--dropdown [data-value='" + year + "']");
        WebElement yearOption = wait.until(ExpectedConditions.elementToBeClickable(yearOptionSelector));

        // Click on the option to select it
        yearOption.click();

        // Wait for the appearance of the button with the class "js-filter-items btn
        // btn--red"
        WebElement filterButton = wait
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector(".js-filter-items.btn.btn--red")));

        // Click the button
        filterButton.click();
    }

    // Function to scrape table data for the first days of each month
    private Map<String, List<String>> scrapeTableDataForFirstDays(WebDriver driver, String tableClass)
            throws InterruptedException {
        Map<String, List<String>> tableData = new LinkedHashMap<>();
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollBy(0, 500);");

        // Wait for the new data to load
        Thread.sleep(200);

        // Find the table element by class
        WebElement table = driver.findElement(By.className(tableClass));

        // Find all rows in the table
        List<WebElement> rows = table.findElements(By.tagName("tr"));

        // Map to track whether we have already fetched data for a given month
        Map<Integer, Boolean> monthDataFetched = new HashMap<>();

        // Loop through rows
        for (int i = 0; i < rows.size(); i++) {
            // Get all cells in the current row
            List<WebElement> cells = rows.get(i).findElements(By.tagName("td"));

            // If the row has at least two cells and the first cell contains a date
            if (cells.size() >= 2) {
                LocalDate currentDate = parseDate(cells.get(0).getText());
                int month = currentDate.getMonthValue();

                // If we haven't fetched data for the current month, add to the map
                if (!monthDataFetched.containsKey(month)) {
                    String key = formatToYearMonth(cells.get(0).getText());
                    List<String> value = new ArrayList<>();
                    value.add(cells.get(1).getText().replaceAll("\\s", ""));
                    tableData.put(key, value);
                    monthDataFetched.put(month, true);
                }
            }
        }
        js.executeScript("window.scrollBy(0, -500);");
        return tableData;
    }

    // Function to format a date to year-month
    private static String formatToYearMonth(String date) {
        LocalDate localDate = parseDate(date);
        return localDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    // Function to parse a date
    private static LocalDate parseDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    public void printData(Map<String, List<String>> data) {
        System.out.println("Printing Data:");
        for (Map.Entry<String, List<String>> entry : data.entrySet()) {
            System.out.println("Year-Month: " + entry.getKey());
            List<String> values = entry.getValue();
            System.out.println("Fuel Price: " + values.get(0));
            System.out.println("---------------");
        }
    }
}
