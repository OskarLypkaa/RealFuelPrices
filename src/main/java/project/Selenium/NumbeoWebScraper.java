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

public class NumbeoWebScraper extends WebScraper {

    @Override
    public Map<String, List<String>> fetchData(String URLAddress) throws WSDataException {

        Map<String, List<String>> finaleDataMap = new HashMap<>();

        // https://www.numbeo.com/cost-of-living/prices_by_country.jsp?displayCurrency=USD&itemId=105
        // https://www.numbeo.com/cost-of-living/prices_by_country.jsp?displayCurrency=USD&itemId=24

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

            // Fetch data from the first table
            finaleDataMap = fetchDataFromTable(driver, "t2");
       
        } catch (Exception e) {
            System.err.println("InterruptedException: " + e.getMessage());
            throw new WSDataException("Failed to fetch fuel prices or average salary due to InterruptedException.", e);
        }
        finally {
            // Close the browser
            driver.quit();
        }
        return finaleDataMap;
    }

    private static Map<String, List<String>> fetchDataFromTable(WebDriver driver, String tableID) {
        Map<String, List<String>> data = new HashMap<>();

        // Locate the table
        WebElement table = driver.findElement(By.id(tableID));

        // Get all rows from the table
        List<WebElement> rows = table.findElements(By.tagName("tr"));
        System.out.println("Starting to fetch table...");
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
        System.out.println("Finished!");
        return data;
    }
}
