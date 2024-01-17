package project.Selenium.Screpers;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import project.Exceptions.WSDataException;
import project.Selenium.WebScraper;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;

public class StooqWebScrapper extends WebScraper {

    @Override
    public Map<String, List<String>> fetchData(String URL) throws WSDataException {
        // Get data from the table with a specified class
        Map<String, List<String>> tableData = new LinkedHashMap<>();

        // Initialize Chrome browser
        WebDriver driver = initializeChromeDriver();

        try {
            // Open the website
            driver.get(URL);

            // Wait for the "I accept" (cookie consent) button to appear and click it
            WebDriverWait wait = new WebDriverWait(driver, 10);
            WebElement acceptButton = wait.until(ExpectedConditions
                    .elementToBeClickable(By.className("fc-button-label")));

            if (acceptButton.isDisplayed()) {
                acceptButton.click();
            }
            int pageNumber = 2;
            // Iterate through years, select each year, and scrape data
            do {
                tableData.putAll(scrapeTableDataForAllDays(driver));
                clickNextPage(driver, pageNumber);
                pageNumber++;
            } while (checkTheTableLatestYear(driver));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.SEVERE, "InterruptedException: {0}", e.getMessage());
            throw new WSDataException("Failed to fetch fuel prices due to InterruptedException.", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception: {0}", e.getMessage());
            throw new WSDataException("Failed to fetch fuel prices.", e);
        } finally {
            // Close the browser
            if (driver != null) {
                driver.quit();
            }
        }
        logger.log(Level.INFO, "Fuel prices received successfully!");

        return tableData;
    }

    // Metoda do klikania w przycisk ">"
    private void clickNextPage(WebDriver driver, int pageNumber) {
        // Zbuduj selektor CSS dla hiperłącza z odpowiednim numerem strony
        String selector = String.format("a[href*='cpiypl.m&i=d&l=%d']", pageNumber);

        // Znajdź element hiperłącza
        WebElement nextPageLink = driver.findElement(By.cssSelector(selector));

        // Kliknij w hiperłącze
        nextPageLink.click();
    }

    // Function to check if any date in the table is after 2004-04-01
    private boolean checkTheTableLatestYear(WebDriver driver) throws InterruptedException {
        // Wait for the new data to load
        Thread.sleep(100);

        // Find the table element by class
        WebDriverWait wait = new WebDriverWait(driver, 10);
        WebElement tableElement = wait
                .until(ExpectedConditions.visibilityOfElementLocated(By.id("fth1")));

        // Find all rows in the table
        List<WebElement> rows = tableElement.findElements(By.tagName("tr"));

        // Loop through rows
        for (int i = 0; i < rows.size(); i++) {
            if (i != 0) {
                // Get all cells in the current row
                List<WebElement> cells = rows.get(i).findElements(By.tagName("td"));

                // If the row has at least two cells and the first cell contains a date
                if (cells.size() >= 2) {
                    String key = formatToYearMonth(cells.get(1).getText());

                    if (LocalDate.parse(key + "-01").isAfter(LocalDate.of(2004, 4, 1)))
                        return true;
                }
            }
        }
        return false;
    }

    // Function to scrape table data for all days
    private Map<String, List<String>> scrapeTableDataForAllDays(WebDriver driver)
            throws InterruptedException {
        Map<String, List<String>> tableData = new LinkedHashMap<>();

        // Find the table element by class
        WebDriverWait wait = new WebDriverWait(driver, 10);
        WebElement tableElement = wait
                .until(ExpectedConditions.visibilityOfElementLocated(By.id("fth1")));

        // Find all rows in the table
        List<WebElement> rows = tableElement.findElements(By.tagName("tr"));

        // Loop through rows
        for (int i = 0; i < rows.size(); i++) {
            if (i != 0) {
                // Get all cells in the current row
                List<WebElement> cells = rows.get(i).findElements(By.tagName("td"));

                // If the row has at least two cells and the first cell contains a date
                if (cells.size() >= 2) {
                    String key = formatToYearMonth(cells.get(1).getText());

                    if (LocalDate.parse(key + "-01").isBefore(LocalDate.of(2004, 4, 1)))
                        return tableData;
                    List<String> value = new ArrayList<>();
                    value.add(cells.get(2).getText());
                    tableData.put(key, value);
                }
            }
        }
        return tableData;
    }

    public static String formatToYearMonth(String inputDate) {
        try {
            // Tworzenie obiektu SimpleDateFormat dla obecnego formatu daty
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMM yyyy");

            // Parsowanie daty z wejściowego ciągu znaków
            Date date = inputFormat.parse(inputDate);

            // Tworzenie obiektu SimpleDateFormat dla nowego formatu daty
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM");

            // Formatowanie daty do nowego formatu
            String outputDate = outputFormat.format(date);

            return outputDate;

        } catch (ParseException e) {
            e.printStackTrace();
            // Obsługa błędów parsowania daty
            return null;
        }
    }
}
