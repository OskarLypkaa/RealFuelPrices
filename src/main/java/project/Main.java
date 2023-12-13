package project;

import project.API.CurrencyExchangeApiClient;
import project.API.OilPriceApiClient;
import project.Selenium.OrlenWebScraper;
import project.analysis.OilPriceAnalyzer;
import project.exceptions.APIStatusException;
import project.exceptions.WSDataException;

public class Main {
    public static void main(String[] args) throws APIStatusException, InterruptedException, WSDataException {
        long startTime = System.currentTimeMillis();

        OilPriceAnalyzer oilPriceInPLN = new OilPriceAnalyzer(
                OilPriceApiClient.fetchOilPriceInUSD(),
                CurrencyExchangeApiClient.fetchExchangeRate());

        oilPriceInPLN.printData(OrlenWebScraper.fetchFuelPriceInPLN());

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("Time taken: " + duration + " milliseconds");
        // JAn Bujas
    }
}
