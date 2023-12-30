package project.Selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import project.API.ApiClient;
import project.exceptions.WSDataException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public abstract class WebScraper {

    protected static final Logger logger = Logger.getLogger(WebScraper.class.getName());

    static {
        try {
            File logsFolder = new File("Logs");
            if (!logsFolder.exists()) {
                logsFolder.mkdirs();
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String logFileName = String.format("Logs/%s_%s.log", ApiClient.class.getSimpleName(), dateFormat.format(new Date()));
            FileHandler fileHandler = new FileHandler(logFileName);

            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected Logger getLogger() {
        return logger;
    }

    public abstract Map<String, List<String>> fetchData(String dataSource) throws WSDataException;

    protected WebDriver initializeChromeDriver() {
        // Common code to initialize ChromeDriver with options

        String workingDirectory = System.getProperty("user.dir");
        Path chromeDriverPath = Paths.get(workingDirectory, "src", "main", "java", "project",
                "Selenium", "webdriver", "chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless"); <- Orlen web scraper is not working with
        // this option, for unknown reason. Subject to verify.

        System.setProperty("webdriver.chrome.driver", chromeDriverPath.toString());

        logger.log(Level.INFO, "Initializing ChromeDriver with options: {0}", options);

        return new ChromeDriver(options);
    }
}
