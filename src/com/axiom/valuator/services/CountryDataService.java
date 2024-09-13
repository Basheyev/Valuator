package com.axiom.valuator.services;


import com.axiom.valuator.math.FinancialMath;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.NumberFormat;
import java.time.Year;
import java.util.Locale;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.http.HttpClient.newHttpClient;

/**
 * Country Open Data API caller service
 */
public class CountryDataService {

    public static final String ERROR_MESSAGE = "Invalid country ISO Alpha-2 code: ";
    public static final String WORLD_BANK_URL = "https://api.worldbank.org/v2/country/";
    public static final String WORLD_BANK_REAL_GDP = "NY.GDP.MKTP.KD";
    public static final String WORLD_BANK_INFLATION = "NY.GDP.DEFL.KD.ZG";
    public static final String WORLD_BANK_BASE_RATE = "NY.GDP.DEFL.KD.ZG"; // todo collect base rate

    private final Locale country;
    private final int YEARS_OF_HISTORY;
    private final NumberFormat currencyFormatter;

    private final String WORLD_BANK_API;
    private final double[] gdpValues;
    private final double[] inflationValues;
    private final double averageGDPGrowthRate;
    private final double averageInflationRate;
    private final int lastYear;
    private final int firstYear;
    private final double corporateTax;
    private final double interestRate;
    private final double marketReturnRate;


    /**
     * Constructor that loads economic data about the country
     * (GDP, Inflation, Corporate Tax) from open data sources
     * @param countryLocale country
     * @param howManyYears how many years of history to load
     */
    public CountryDataService(Locale countryLocale, int howManyYears) {

        if (countryLocale == null) throw new IllegalArgumentException(ERROR_MESSAGE);
        if (howManyYears < 1) throw new IllegalArgumentException(ERROR_MESSAGE);
        this.country = countryLocale;
        YEARS_OF_HISTORY = howManyYears;

        // initialize currency formatter
        currencyFormatter = NumberFormat.getCurrencyInstance(countryLocale);
        currencyFormatter.setMaximumFractionDigits(0);

        // Initialize constants
        WORLD_BANK_API = WORLD_BANK_URL + countryLocale.getCountry() + "/indicator/";
        lastYear = Year.now().getValue() - 1;
        firstYear = lastYear - (YEARS_OF_HISTORY - 1);

        // Load country GDP data
        gdpValues = new double[YEARS_OF_HISTORY];
        fetchRealGDPData();
        int periods = gdpValues.length - 1;
        double startValue = gdpValues[0];
        double endValue = gdpValues[periods];
        averageGDPGrowthRate = FinancialMath.getCAGR(startValue, endValue, periods);

        // Load country inflation data
        inflationValues = new double[YEARS_OF_HISTORY];
        fetchInflationData();
        double sum = 0;
        for (double inflationValue : inflationValues) {
            if (inflationValue == 0) break;
            sum += inflationValue;
        }
        averageInflationRate = (sum / inflationValues.length);

        // load base rate
        interestRate = fetchInterestRate();
        marketReturnRate = 0.2493; // todo fetch market return rate

        // Load country corporate tax
        corporateTax = CountryTaxService.taxRate(countryLocale);
    }


    /**
     * Fetches GDP data from World Bank for YEARS_OF_HISTORY period
     */
    private void fetchRealGDPData() {
        String url = WORLD_BANK_API + WORLD_BANK_REAL_GDP + "?date=" + firstYear + ":" + lastYear + "&format=json";
        String jsonResponse = getRequest(url);
        if (jsonResponse==null) throw new IllegalArgumentException("Can't fetch data from " + url);
        JSONArray response = new JSONArray(jsonResponse);
        JSONArray jsonArray = (JSONArray) response.get(1);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject gdpEntry = jsonArray.getJSONObject(i);
            int year = Integer.parseInt(gdpEntry.getString("date"));
            int index = year - firstYear;
            double value = gdpEntry.isNull("value") ? 0.0d : gdpEntry.getDouble("value");
            gdpValues[index] = value;
        }
    }


    /**
     * Fetches inflation data from World Bank for YEARS_OF_HISTORY period
     */
    private void fetchInflationData() {
        String url = WORLD_BANK_API + WORLD_BANK_INFLATION + "?date=" + firstYear + ":" + lastYear + "&format=json";
        String jsonResponse = getRequest(url);
        if (jsonResponse==null) throw new IllegalArgumentException("Can't fetch data from " + url);
        JSONArray response = new JSONArray(jsonResponse);
        JSONArray jsonArray = (JSONArray) response.get(1);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject gdpEntry = jsonArray.getJSONObject(i);
            int year = Integer.parseInt(gdpEntry.getString("date"));
            int index = year - firstYear;
            double value = gdpEntry.isNull("value") ? 0.0d : gdpEntry.getDouble("value");
            inflationValues[index] = value / 100.0;
        }
    }


    private double fetchInterestRate() {
        String url = WORLD_BANK_API + WORLD_BANK_BASE_RATE + "?date=" + (lastYear-1) + "&format=json";

        String jsonResponse = getRequest(url);
        if (jsonResponse==null) throw new IllegalArgumentException("Can't fetch data from " + url);

        // todo fetch interest rate

        //System.out.println("BASE RATE:" + jsonResponse);

        return 0.1425;
    }


    public String getCountryCode() {
        return country.getCountry();
    }

    public String getCountryName() {
        return country.getDisplayCountry();
    }

    public int getFirstYear() {
        return firstYear;
    }

    public int getLastYear() {
        return lastYear;
    }


    public double getAverageGDPGrowthRate() {
        return averageGDPGrowthRate;
    }


    public double getGDP(int year) {
        int index = year - firstYear;
        if (index < 0 || index >= gdpValues.length) return Double.NaN;
        return gdpValues[index];
    }


    public double getAverageInflationRate() {
        return averageInflationRate;
    }

    public double getInflation(int year) {
        int index = year - firstYear;
        if (index < 0 || index >= inflationValues.length) return Double.NaN;
        return inflationValues[index];
    }

    public double getCorporateTax() {
        return corporateTax;
    }

    public double getRiskFreeRate() {
        return interestRate;
    }

    public double getMarketReturn() {
        return marketReturnRate;
    }

    /**
     * Validates country ISO3166 Alpha-2 code
     * @param ISOAlpha2code country ISO3166 Alpha-2 code
     * @return locale if valid country code, null otherwise
     */
    public static Locale getCountryByCode(String ISOAlpha2code) {
        if (ISOAlpha2code == null || ISOAlpha2code.length() != 2) return null;
        String upperCode = ISOAlpha2code.toUpperCase();
        for (Locale locale : Locale.getAvailableLocales()) {
            if (upperCode.equals(locale.getCountry())) return locale;
        }
        return null;
    }


    /**
     * Sends HTTP request
     * @param URL required URL
     * @return response body
     */
    private String getRequest(String URL) {
        HttpClient client = newHttpClient();
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


    public String formatMoney(double moneyValue) {
        return currencyFormatter.format(moneyValue);
    }


    @Override
    public String toString() {

        Locale region = CountryDataService.getCountryByCode("US");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(region);
        StringBuilder sb = new StringBuilder();

        sb.append(getCountryName())
            .append(" (")
            .append(country.getCountry())
            .append(")\n");

        for (int i = 0; i < YEARS_OF_HISTORY; i++) {
            sb.append(firstYear + i)
                .append(" GDP: ")
                .append(currencyFormatter.format(gdpValues[i]))
                .append(" (");
            if (i>0) {
                sb.append("growth ");
                sb.append(Math.round((gdpValues[i] / gdpValues[i-1] - 1) * 10000.0) / 100.0);
                sb.append("%, ");
            }
            sb.append("inflation ")
                .append(Math.round(inflationValues[i] * 10000.0) / 100.0)
                .append("%)\n");
        }


        sb.append("Average GDP growth rate: ")
            .append(Math.round(getAverageGDPGrowthRate() * 10000.0) / 100.0)
            .append("%\n");

        sb.append("Average Inflation Rate: ")
            .append(Math.round(getAverageInflationRate() * 10000.0) / 100.0)
            .append("%\n");

        sb.append("Interest Rate: ")
            .append(Math.round(getRiskFreeRate() * 10000.0) / 100.0)
            .append("%\n");

        sb.append("Corporate Tax Rate: ")
            .append(Math.round(getCorporateTax() * 10000.0) / 100.0)
            .append("%");

        return sb.toString();
    }
}
