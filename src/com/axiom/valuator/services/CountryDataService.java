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
    public static final String WORLD_BANK_INFLATION = "";
    public static final int YEARS_OF_HISTORY = 15;

    private final String WORLD_BANK_API;
    private final Locale country;
    private final double[] gdpValues;
    private final int gdpLastYear;
    private final int gdpFirstYear;

    public CountryDataService(String countryCode) {
        country = CountryDataService.getCountryByCode(countryCode);
        if (country == null) throw new IllegalArgumentException(ERROR_MESSAGE + countryCode);

        // Load country GDP data
        WORLD_BANK_API = WORLD_BANK_URL + countryCode + "/indicator/";
        gdpValues = new double[YEARS_OF_HISTORY];
        gdpLastYear = CountryDataService.lastYear();
        gdpFirstYear = gdpLastYear - (YEARS_OF_HISTORY - 1);
        loadRealGDPData();

        // Load country inflation data

    }

    public int getGDPFirstYear() {
        return gdpFirstYear;
    }

    public int getGDPLastYear() {
        return gdpLastYear;
    }

    public double getGDPValue(int year) {
        int index = year - gdpFirstYear;
        if (index < 0 || index >= gdpValues.length) return Double.NaN;
        return gdpValues[index];
    }

    public String getCountry() {
        return country.getCountry();
    }


    public double getCorporateTax() {
        return 0.2;
    }



    private boolean loadRealGDPData() {

        String url = WORLD_BANK_API + WORLD_BANK_REAL_GDP + "?date=" + gdpFirstYear + ":" + gdpLastYear + "&format=json";
        String jsonResponse = getRequest(url, null);
        if (jsonResponse==null) return false;

        JSONArray response = new JSONArray(jsonResponse);
         JSONArray jsonArray = (JSONArray) response.get(1);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject gdpEntry = jsonArray.getJSONObject(i);
            int year = Integer.parseInt(gdpEntry.getString("date"));
            int index = year - gdpFirstYear;
            gdpValues[index] = gdpEntry.getDouble("value");
        }


        return true;
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


    private static int lastYear() {
        return Year.now().getValue() - 1;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(country.getCountry());
        sb.append("\n");
        Locale region = CountryDataService.getCountryByCode("US");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(region);
        for (int i = 0; i < YEARS_OF_HISTORY; i++) {
            sb.append("Year: " + (gdpFirstYear + i) + ", GDP: " + currencyFormatter.format(gdpValues[i]) + "\n");
        }

        return sb.toString();
    }
}
