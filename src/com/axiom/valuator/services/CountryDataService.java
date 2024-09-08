package com.axiom.valuator.services;


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
 * World Bank Open API caller service
 */
public class CountryDataService {

    public static final String ERROR_MESSAGE = "Invalid country ISO Alpha-2 code: ";
    public static final String WORLD_BANK_URL = "https://api.worldbank.org/v2/country/";
    public static final String WORLD_BANK_REAL_GDP = "NY.GDP.MKTP.KD";
    public static final String WORLD_BANK_INFLATION = "FP.CPI.TOTL.ZG";
    public static final int YEARS_OF_HISTORY = 10;

    private final String WORLD_BANK_API;
    private final Locale country;
    private final double[] gdpValues;
    private final double[] inflationValues;
    private final int lastYear;
    private final int firstYear;

    public CountryDataService(String countryCode) {
        country = CountryDataService.getCountryByCode(countryCode);
        if (country == null) throw new IllegalArgumentException(ERROR_MESSAGE + countryCode);

        // Initialize constants
        WORLD_BANK_API = WORLD_BANK_URL + countryCode + "/indicator/";
        lastYear = Year.now().getValue() - 1;
        firstYear = lastYear - (YEARS_OF_HISTORY - 1);

        // Load country GDP data
        gdpValues = new double[YEARS_OF_HISTORY];
        loadRealGDPData();

        // Load country inflation data
        inflationValues = new double[YEARS_OF_HISTORY];
        loadInflationData();
    }


    private void loadRealGDPData() {
        String url = WORLD_BANK_API + WORLD_BANK_REAL_GDP + "?date=" + firstYear + ":" + lastYear + "&format=json";
        String jsonResponse = getRequest(url, null);
        if (jsonResponse==null) return;
        JSONArray response = new JSONArray(jsonResponse);
        JSONArray jsonArray = (JSONArray) response.get(1);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject gdpEntry = jsonArray.getJSONObject(i);
            int year = Integer.parseInt(gdpEntry.getString("date"));
            int index = year - firstYear;
            double value = gdpEntry.isNull("value") ? Double.NaN : gdpEntry.getDouble("value");
            gdpValues[index] = value;
        }
    }


    private void loadInflationData() {
        String url = WORLD_BANK_API + WORLD_BANK_INFLATION + "?date=" + firstYear + ":" + lastYear + "&format=json";
        String jsonResponse = getRequest(url, null);
        if (jsonResponse==null) return;
        JSONArray response = new JSONArray(jsonResponse);
        JSONArray jsonArray = (JSONArray) response.get(1);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject gdpEntry = jsonArray.getJSONObject(i);
            int year = Integer.parseInt(gdpEntry.getString("date"));
            int index = year - firstYear;
            double value = gdpEntry.isNull("value") ? Double.NaN : gdpEntry.getDouble("value");
            inflationValues[index] = value;
        }
    }


    public String getCountry() {
        return country.getDisplayCountry();
    }


    public int getFirstYear() {
        return firstYear;
    }

    public int getLastYear() {
        return lastYear;
    }

    public double getGDPValue(int year) {
        int index = year - firstYear;
        if (index < 0 || index >= gdpValues.length) return Double.NaN;
        return gdpValues[index];
    }

    public double getCorporateTax() {
        return 0.2;
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
     * @param token authorization token
     * @return response body
     */
    private String getRequest(String URL, String token) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(URL))
            .header("Authorization", "Bearer " + token)
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


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(getCountry() + " (" + country.getCountry() + ")");
        sb.append("\n");
        Locale region = CountryDataService.getCountryByCode("US");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(region);
        for (int i = 0; i < YEARS_OF_HISTORY; i++) {
            sb.append((firstYear + i) + " GDP: " + currencyFormatter.format(gdpValues[i]));
            sb.append("  inflat: " + (Math.round(inflationValues[i] * 100.0) / 100.0) + "%");
            sb.append("\n");
        }

        return sb.toString();
    }
}
