package project.Selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import project.exceptions.WSDataException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class WebScraper {

    private static final Logger logger = Logger.getLogger(WebScraper.class.getName());

    public abstract Map<String, List<String>> fetchData(String dataSource) throws WSDataException;

    protected WebDriver initializeChromeDriver() {
        // Common code to initialize ChromeDriver with options

        String workingDirectory = System.getProperty("user.dir");
        Path chromeDriverPath = Paths.get(workingDirectory, "src", "main", "java", "project",
                "Selenium", "webdriver", "chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless"); <- Orlen web scraper is not working with
        // this option, for unknown reason. Subject t to verify.

        System.setProperty("webdriver.chrome.driver", chromeDriverPath.toString());

        logger.log(Level.INFO, "Initializing ChromeDriver with options: {0}", options);

        return new ChromeDriver(options);
    }
}
