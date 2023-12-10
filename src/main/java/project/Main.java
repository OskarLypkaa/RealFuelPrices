package project;

import project.API.CurrencyExchangeApiClient;
import project.API.OilPriceApiClient;
import project.exceptions.APIStatusException;

public class Main {
    public Main() {
    }

    public static void main(String[] args) throws APIStatusException {

        OilPriceApiClient oilPrice = new OilPriceApiClient();

        // oilPrice.fetchOilPriceInUSD();

        CurrencyExchangeApiClient currencyExchange = new CurrencyExchangeApiClient();

        currencyExchange.fetchExchangeRate();
    }
}
