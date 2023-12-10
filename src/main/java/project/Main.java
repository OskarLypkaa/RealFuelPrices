package project;

import java.util.Map;

import project.API.CurrencyExchangeApiClient;
import project.API.OilPriceApiClient;
import project.analysis.OilPriceAnalyzer;
import project.exceptions.APIStatusException;

public class Main {
    public static void main(String[] args) throws APIStatusException {

        OilPriceAnalyzer oilPriceInPLN = new OilPriceAnalyzer(OilPriceApiClient.fetchOilPriceInUSD(),
                CurrencyExchangeApiClient.fetchExchangeRate());

        Map<String, String> analiseResult = oilPriceInPLN.analyzedOilPricesInPLN();

        try {
            for (String date : analiseResult.keySet()) {
                System.out.println("Wartość bartyłki ropy w " + date + ": " + analiseResult.get(date) + "PLN");
                Thread.sleep(1000);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
