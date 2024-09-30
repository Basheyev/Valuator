//=======================================================================================
//
//  PRIVATE COMPANY VALUATOR
//  Public Company Stock Data
//
// (C) 2024 Axiom Capital, Bolat Basheyev
//=======================================================================================
package com.axiom.valuator.model;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.NumberFormat;
import java.util.Locale;

import static java.net.HttpURLConnection.HTTP_OK;


/**
 * Fetches and stores public company stock data and financials
 * used in comparable analysis, especially EV/Revenue, EV/EBITDA
 */
public class StockData {

    public static final String API_URL = "https://www.alphavantage.co/query?function=OVERVIEW&symbol=";
    public static final String API_KEY = "ZT4F55HUBU1JD2CF";

    public static final String ERROR_NULL_RESPONSE = "Can't request ";
    public static final String ERROR_EMPTY_OBJECT = "Empty object returned from ";
    public static final String INFORMATION_FIELD = "Information";

    private JSONObject stock;



    /**
     * Stock Data Constructor
     * @param symbol public company ticker
     */
    public StockData(String symbol) {

        // Check cache to avoid redundant requests because Alpha Vantage limits to 25 requests per day
        boolean alreadyCached = false;
        if (CachedData.containsCompany(symbol)) {
            String jsonString = CachedData.getCompany(symbol);
            if (jsonString != null) {
                stock = new JSONObject(jsonString);  // let's throw exception if null for debug purposes
                alreadyCached = true;
            }
        }

        // if not cached
        if (!alreadyCached) {
            String urlString = API_URL + symbol + "&apikey=" + API_KEY;
            String response = getRequest(urlString);
            if (response == null) throw new IllegalArgumentException(ERROR_NULL_RESPONSE + urlString);
            stock = new JSONObject(response);
            if (stock.isEmpty())
                throw new IllegalArgumentException(ERROR_EMPTY_OBJECT + urlString);
            else if (stock.has(INFORMATION_FIELD))
                throw new IllegalArgumentException(stock.getString(INFORMATION_FIELD));
            CachedData.putCompany(symbol, stock.toString());
        }
    }



    /**
     * Sends GET request and returns response
     * @param URL request URL
     * @return response string or null if an exception occurs
     */
    private String getRequest(String URL) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL)).GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            if (statusCode != HTTP_OK) throw new IOException("Status code " + statusCode + " for URL=" + URL);
            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public String getSymbol() { return stock.getString("Symbol"); }
    public String getName() { return stock.getString("Name"); }
    public double getRevenueTTM() { return stock.getDouble("RevenueTTM");  }
    public double getEBITDA() { return stock.getDouble("EBITDA"); }
    public double getOperatingMarginTTM() { return stock.getDouble("OperatingMarginTTM"); }
    public double getGrossProfitTTM() { return stock.getDouble("GrossProfitTTM"); }
    public double getEVToRevenue() { return stock.getDouble("EVToRevenue"); }
    public double getEVToEBITDA() { return stock.getDouble("EVToEBITDA"); }
    public double getMarketCapitalization() { return stock.getDouble("MarketCapitalization"); }
    public double getEnterpriseValue() { return getEBITDA() * getEVToEBITDA(); }
    public double getEarningsPerShare() { return stock.getDouble("EPS"); }
    public double getRevenuePerShareTTM() { return stock.getDouble("RevenuePerShareTTM"); }


    @Override
    public String toString() {
        Locale region = CountryData.getCountryByCode("US");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(region);
        return getName() + " (" + getSymbol() + ")\n" +
            "Revenue (TTM): " + currencyFormatter.format(getRevenueTTM()) + "\n" +
            "EBITDA (TTM): " + currencyFormatter.format(getEBITDA()) + "\n" +
            "Gross Profit (TTM): " + currencyFormatter.format(getGrossProfitTTM()) + "\n" +
            "Market Capitalization: " + currencyFormatter.format(getMarketCapitalization()) + "\n" +
            "Enterprise Value: " + currencyFormatter.format(getEnterpriseValue()) + "\n" +
            "EV\\Revenue: " + getEVToRevenue() + "x\n" +
            "EV\\EBITDA: " + getEVToEBITDA() + "x\n";
    }
}
