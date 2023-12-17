package project.Selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import project.exceptions.WSDataException;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.Map;

public abstract class WebScraper {

    public abstract Map<String, List<String>> fetchData(String URLAddress) throws WSDataException;

    protected WebDriver initializeChromeDriver() {
        // Common code to initialize ChromeDriver with options

        String workingDirectory = System.getProperty("user.dir");
        Path chromeDriverPath = Paths.get(workingDirectory, "RealFuelPrices", "src", "main", "java", "project", "Selenium", "webdriver", "chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");

        System.setProperty("webdriver.chrome.driver", chromeDriverPath.toString());

        return new ChromeDriver(options);
    }
}
