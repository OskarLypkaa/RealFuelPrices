package project.Selenium.Screpers;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import project.Exceptions.WSDataException;
import project.Selenium.WebScraper;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;

public class OrlenWebScraper extends WebScraper {

    @Override
    public Map<String, List<String>> fetchData(String fuelType) throws WSDataException {
        // Get data from the table with a specified class
        Map<String, List<String>> tableData = new LinkedHashMap<>();

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

            selectFuelType(driver, fuelType);

            // Iterate through years, select each year, and scrape data
            for (int year = LocalDate.now().getYear(); year >= 2004; year--) {
                selectYear(driver, year);
                tableData.putAll(scrapeTableDataForAllDays(driver, "table--effectivedate"));
                logger.info("Fetched " + fuelType + " data from year:" + year);
            }

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

        tableData = correctPricesFormat(tableData);
        return tableData;
    }

    private void selectFuelType(WebDriver driver, String fuelType) {
        WebDriverWait wait = new WebDriverWait(driver, 10);

        // Find the dropdown element
        List<WebElement> dropdowns = driver.findElements(By.className("choices"));

        // Click on the dropdown to expand the options
        dropdowns.get(0).click();

        // Define the dynamic fuel option selector based on the fuel type
        By fuelOptionSelector = By.xpath("//div[text()='" + fuelType + "']");

        WebElement fuelOption = wait.until(ExpectedConditions.elementToBeClickable(fuelOptionSelector));

        // Click on the option to select it
        fuelOption.click();
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

        // Scroll to the year option
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", yearOption);
        yearOption.click();
        // Wait for the appearance of the button with the class "js-filter-items btn
        // btn--red"
        WebElement filterButton = wait
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector(".js-filter-items.btn.btn--red")));

        // Click the button
        filterButton.click();
    }

    // Function to scrape table data for all days
    private Map<String, List<String>> scrapeTableDataForAllDays(WebDriver driver, String tableClass)
            throws InterruptedException {
        Map<String, List<String>> tableData = new LinkedHashMap<>();
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollBy(0, 500);");

        // Wait for the new data to load
        Thread.sleep(200);

        // Find the table element by class
        WebDriverWait wait = new WebDriverWait(driver, 10);
        WebElement tableElement = wait
                .until(ExpectedConditions.visibilityOfElementLocated(By.className("table--effectivedate")));

        // Find all rows in the table
        List<WebElement> rows = tableElement.findElements(By.tagName("tr"));

        // Loop through rows
        for (int i = 0; i < rows.size(); i++) {
            // Get all cells in the current row
            List<WebElement> cells = rows.get(i).findElements(By.tagName("td"));

            // If the row has at least two cells and the first cell contains a date
            if (cells.size() >= 2) {
                String key = formatToYearMonthDay(cells.get(0).getText());

                if (LocalDate.parse(key).isBefore(LocalDate.of(2004, 4, 1)))
                    return tableData;
                List<String> value = new ArrayList<>();
                value.add(cells.get(1).getText().replaceAll("\\s", ""));
                tableData.put(key, value);
            }
        }
        js.executeScript("window.scrollBy(0, -1000);");
        return tableData;
    }

    // Function to format a date to year-month
    private static String formatToYearMonthDay(String date) {
        LocalDate localDate = parseDate(date);
        return localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    // Function to parse a date
    private static LocalDate parseDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    private static Map<String, List<String>> correctPricesFormat(Map<String, List<String>> data) {
        Map<String, List<String>> result = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> entry : data.entrySet()) {
            List<String> originalPrices = entry.getValue();
            List<String> formattedPrices = new ArrayList<>();

            for (String originalPrice : originalPrices) {
                String formattedPrice = formatPrice(originalPrice);
                formattedPrices.add(formattedPrice);
            }

            result.put(entry.getKey(), formattedPrices);
        }

        return result;
    }

    private static String formatPrice(String originalPrice) {
        try {
            long priceValue = Long.parseLong(originalPrice);
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setGroupingSeparator('.');

            DecimalFormat df = new DecimalFormat("#,###", symbols);
            return df.format(priceValue);
        } catch (NumberFormatException e) {
            return originalPrice;
        }
    }
}
