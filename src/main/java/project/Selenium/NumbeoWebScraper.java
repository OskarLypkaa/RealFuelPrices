package project.Selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import project.exceptions.WSDataException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;


public class NumbeoWebScraper extends WebScraper {

    @Override
    public Map<String, List<String>> fetchData(String URLAddress) throws WSDataException {

        Map<String, List<String>> finaleDataMap = new HashMap<>();

        // Initialize Chrome browser
        WebDriver driver = initializeChromeDriver();

        try {
            // Open the website
            driver.get(URLAddress);

            // Accept choices
            WebElement acceptChoicesButton = driver.findElement(By.id("accept-choices"));
            acceptChoicesButton.click();

            // Scroll down to reveal the table
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollBy(0, 500);");

            // Wait for the table to load
            WebDriverWait wait = new WebDriverWait(driver, 10);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("t2")));

            // Fetch data from the table
            finaleDataMap = fetchDataFromTable(driver, "t2");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception: {0}", e.getMessage());
            throw new WSDataException("Failed to fetch data from Numbeo website.", e);
        } finally {
            // Close the browser
            if (driver != null) {
                driver.quit();
            }
        }
        logger.log(Level.INFO, "Data received successfully from Numbeo website!");
        return finaleDataMap;
    }

    private static Map<String, List<String>> fetchDataFromTable(WebDriver driver, String tableID) {
        Map<String, List<String>> data = new HashMap<>();

        // Locate the table
        WebElement table = driver.findElement(By.id(tableID));

        // Get all rows from the table
        List<WebElement> rows = table.findElements(By.tagName("tr"));
        logger.log(Level.INFO, "Starting to fetch table...");

        // Iterate over rows, starting from the second row (skip header)
        for (int i = 1; i < rows.size(); i++) {
            WebElement row = rows.get(i);
            List<WebElement> cells = row.findElements(By.tagName("td"));
            List<String> listOfPrices = new LinkedList<>();

            // Extract country name, fuel price, and income
            String country = cells.get(1).getText();
            String tableValue = cells.get(2).getText();

            listOfPrices.add(tableValue);

            // Store data in the map
            data.put(country, listOfPrices);
        }

        logger.log(Level.INFO, "Finished fetching table!");
        return data;
    }
}
