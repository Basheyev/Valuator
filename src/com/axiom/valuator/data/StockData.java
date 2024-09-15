package com.axiom.valuator.data;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.NumberFormat;
import java.util.Locale;

import static java.net.HttpURLConnection.HTTP_OK;


// fixme: Alpha Vantage limits 25 requests per day
public class StockData {

    public static final String API_URL = "https://www.alphavantage.co/query";
    public static final String API_KEY = "ZT4F55HUBU1JD2CF";

    private final JSONObject stock;

    public StockData(String symbol) {

        String urlString = API_URL + "?function=OVERVIEW&symbol=" + symbol + "&apikey=" + API_KEY;
        String response = getRequest(urlString);
        if (response==null) throw new IllegalArgumentException("Can't request " + urlString);
        stock = new JSONObject(response);
        if (stock.isEmpty()) {
            throw new IllegalArgumentException("Empty object returned from " + urlString);
        } else if (stock.has("Information")) {
            throw new IllegalArgumentException(stock.getString("Information"));
        }

    }

    private String getRequest(String URL) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(URL))
            .GET().build();

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


    public String getSymbol() {
        return stock.getString("Symbol");
    }

    public String getName() {
        return stock.getString("Name");
    }

    public double getRevenueTTM() {
        return stock.getDouble("RevenueTTM");
    }

    public double getEBITDA() {
        return stock.getDouble("EBITDA");
    }

    public double getOperatingMarginTTM() {
        return stock.getDouble("OperatingMarginTTM");
    }

    public double getGrossProfitTTM() {
        return stock.getDouble("GrossProfitTTM");
    }

    public double getEVToRevenue() {
        return stock.getDouble("EVToRevenue");
    }

    public double getEVToEBITDA() {
        return stock.getDouble("EVToEBITDA");
    }

    public double getMarketCapitalizaion() {
        return stock.getDouble("MarketCapitalization");
    }

    public double getEnterpriseValue() {
        return getEBITDA() * getEVToEBITDA();
    }

    public double getEarningsPerShare() {
        return stock.getDouble("EPS");
    }

    public double getRevenuePerShareTTM() {
        return stock.getDouble("RevenuePerShareTTM");
    }


    @Override
    public String toString() {
        Locale region = CountryData.getCountryByCode("US");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(region);
        StringBuilder sb = new StringBuilder();

        sb.append(getName());
        sb.append(" (");
        sb.append(getSymbol());
        sb.append(")");
        sb.append("\n");
        sb.append("Revenue (TTM): ");
        sb.append(currencyFormatter.format(getRevenueTTM()));
        sb.append("\n");
        sb.append("EBITDA (TTM): ");
        sb.append(currencyFormatter.format(getEBITDA()));
        sb.append("\n");
        sb.append("Gross Profit (TTM): ");
        sb.append(currencyFormatter.format(getGrossProfitTTM()));
        sb.append("\n");
        sb.append("Market Capitalization: ");
        sb.append(currencyFormatter.format(getMarketCapitalizaion()));
        sb.append("\n");
        sb.append("Enterprise Value: ");
        sb.append(currencyFormatter.format(getEnterpriseValue()));
        sb.append("\n");
        sb.append("EV\\Revenue: ");
        sb.append(getEVToRevenue());
        sb.append("x\n");
        sb.append("EV\\EBITDA: ");
        sb.append(getEVToEBITDA());
        sb.append("x\n");

        return sb.toString();
    }
}
