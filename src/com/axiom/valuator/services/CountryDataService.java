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

/**
 * Country Open Data API caller service
 */
public class CountryDataService {

    public static final String ERROR_MESSAGE = "Invalid country ISO Alpha-2 code: ";
    public static final String WORLD_BANK_URL = "https://api.worldbank.org/v2/country/";
    public static final String WORLD_BANK_REAL_GDP = "NY.GDP.MKTP.KD";
    public static final String WORLD_BANK_INFLATION = "NY.GDP.DEFL.KD.ZG"; //"FP.CPI.TOTL.ZG";
    public static final String WORLD_BANK_BASE_RATE = "NY.GDP.DEFL.KD.ZG";


    private final int YEARS_OF_HISTORY;
    private final NumberFormat currencyFormatter;
    private final String WORLD_BANK_API;
    private final Locale country;
    private final double[] gdpValues;
    private final double[] inflationValues;
    private final double averageGDPGrowthRate;
    private final double averageInflationRate;
    private final int lastYear;
    private final int firstYear;
    private final double corporateTax;


    /**
     * Constructor that loads economic data about the country
     * (GDP, Inflation, Corporate Tax) from open data sources
     * @param countryCode ISO Alpha-2 Country Code
     */
    public CountryDataService(String countryCode, int howManyYears) {
        country = CountryDataService.getCountryByCode(countryCode);
        if (country == null) throw new IllegalArgumentException(ERROR_MESSAGE + countryCode);
        if (howManyYears < 1) throw new IllegalArgumentException(ERROR_MESSAGE + countryCode);
        YEARS_OF_HISTORY = howManyYears;

        // initialize currency formatter
        currencyFormatter = NumberFormat.getCurrencyInstance(country);

        // Initialize constants
        WORLD_BANK_API = WORLD_BANK_URL + countryCode + "/indicator/";
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
        fetchBaseRate();

        // Load country corporate tax
        corporateTax = 0.2;
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


    private void fetchBaseRate() {
        String url = WORLD_BANK_API + WORLD_BANK_BASE_RATE + "?date=" + (lastYear-1) + "&format=json";
        String jsonResponse = getRequest(url);
        if (jsonResponse==null) throw new IllegalArgumentException("Can't fetch data from " + url);


        // todo

        System.out.println(jsonResponse);


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
            };
            sb.append("inflation ")
                .append(Math.round(inflationValues[i] * 10000.0) / 100.0)
                .append("%)\n");
        }


        sb.append("Average GDP growth rate: ")
            .append(Math.round(getAverageGDPGrowthRate() * 10000.0) / 100.0)
            .append("%\n");

        sb.append("Average Inflation Rate: ")
            .append(Math.round(getAverageInflationRate() * 10000.0) / 100.0)
            .append("%");

        return sb.toString();
    }
}
