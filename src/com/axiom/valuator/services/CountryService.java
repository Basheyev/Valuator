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
public class CountryService {

    public static final String ERROR_MESSAGE = "Invalid country ISO Alpha-2 code: ";
    public static final String WORLD_BANK_URL = "https://api.worldbank.org/v2/country/";
    public static final String WORLD_BANK_REAL_GDP = "NY.GDP.MKTP.KD";
    public static final String WORLD_BANK_INFLATION = "NY.GDP.DEFL.KD.ZG";

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
    public CountryService(Locale countryLocale, int howManyYears) {

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
        marketReturnRate = DEFAULT_MARKET_RETURN_RATE;

        // Load country corporate tax
        corporateTax = getTaxRate(countryLocale);
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
        String iso3Code = country.getISO3Country();
        for (Object[] tuple: COUNTRY_BASE_RATES) {
            String iso3 = (String) tuple[0];
            double rate = (Double) tuple[1];
            if (iso3.equals(iso3Code)) {
                return rate / 100.0;
            }
        }
        return WORLD_AVERAGE_BASE_RATE;
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

        Locale region = CountryService.getCountryByCode("US");
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

    public static double getTaxRate(Locale country) {
        String iso3Code = country.getISO3Country();
        for (Object[] tuple: COUNTRIES_CORPORATE_TAX_RATES) {
            String iso3 = (String) tuple[0];
            double rate = (Double) tuple[2];
            if (iso3.equals(iso3Code)) {
                return rate / 100.0;
            }
        }
        return WORLD_AVERAGE_CORPORATE_TAX_RATE;
    }

    // Tax data for 2023
    private static final Object[][] COUNTRIES_CORPORATE_TAX_RATES = {
        {"AFG","Afghanistan",20.0},
        {"AGO","Angola",25.0},
        {"ALB","Albania",15.0},
        {"ARE","United Arab Emirates",9.0},
        {"ARG","Argentina",35.0},
        {"ARM","Armenia",18.0},
        {"ATG","Antigua and Barbuda",25.0},
        {"AUS","Australia",30.0},
        {"AUT","Austria",24.0},
        {"AZE","Azerbaijan",20.0},
        {"BDI","Burundi",30.0},
        {"BEL","Belgium",25.0},
        {"BEN","Benin",30.0},
        {"BFA","Burkina Faso",27.5},
        {"BGD","Bangladesh",27.5},
        {"BGR","Bulgaria",10.0},
        {"BHR","Bahrain",0.0},
        {"BHS","Bahamas",0.0},
        {"BIH","Bosnia and Herzegovina",10.0},
        {"BLR","Belarus",20.0},
        {"BLZ","Belize",0.0},
        {"BOL","Bolivia (Plurinational State of)",25.0},
        {"BRA","Brazil",34.0},
        {"BRB","Barbados",5.5},
        {"BRN","Brunei Darussalam",18.5},
        {"BTN","Bhutan",25.0},
        {"BWA","Botswana",22.0},
        {"CAF","Central African Republic",30.0},
        {"CAN","Canada",26.21},
        {"CHE","Switzerland",19.653},
        {"CHL","Chile",27.0},
        {"CHN","China",25.0},
        {"CIV","Cote d'Ivoire",25.0},
        {"CMR","Cameroon",33.0},
        {"COD","Democratic Republic of the Congo",30.0},
        {"COG","Congo",28.0},
        {"COL","Colombia",35.0},
        {"CPV","Cabo Verde",22.44},
        {"CRI","Costa Rica",30.0},
        {"CUB","Cuba",35.0},
        {"CYP","Cyprus",12.5},
        {"CZE","Czechia",19.0},
        {"DEU","Germany",29.941},
        {"DJI","Djibouti",25.0},
        {"DMA","Dominica",25.0},
        {"DNK","Denmark",22.0},
        {"DOM","Dominican Republic",27.0},
        {"DZA","Algeria",26.0},
        {"ECU","Ecuador",25.0},
        {"EGY","Egypt",22.5},
        {"ERI","Eritrea",30.0},
        {"ESP","Spain",25.0},
        {"EST","Estonia",20.0},
        {"ETH","Ethiopia",30.0},
        {"FIN","Finland",20.0},
        {"FJI","Fiji",20.0},
        {"FRA","France",25.825},
        {"GAB","Gabon",30.0},
        {"GBR","United Kingdom of Great Britain and Northern Ireland",25.0},
        {"GEO","Georgia",15.0},
        {"GHA","Ghana",25.0},
        {"GIN","Guinea",25.0},
        {"GMB","Gambia",27.0},
        {"GNB","Guinea-Bissau",25.0},
        {"GNQ","Equatorial Guinea",35.0},
        {"GRC","Greece",22.0},
        {"GRD","Grenada",28.0},
        {"GTM","Guatemala",25.0},
        {"GUY","Guyana",25.0},
        {"HKG","China, Hong Kong Special Administrative Region",16.5},
        {"HND","Honduras",30.0},
        {"HRV","Croatia",18.0},
        {"HTI","Haiti",30.0},
        {"HUN","Hungary",9.0},
        {"IDN","Indonesia",22.0},
        {"IND","India",30.0},
        {"IRL","Ireland",12.5},
        {"IRN","Iran (Islamic Republic of)",25.0},
        {"IRQ","Iraq",15.0},
        {"ISL","Iceland",20.0},
        {"ISR","Israel",23.0},
        {"ITA","Italy",27.81},
        {"JAM","Jamaica",25.0},
        {"JOR","Jordan",20.0},
        {"JPN","Japan",29.74},
        {"KAZ","Kazakhstan",20.0},
        {"KEN","Kenya",30.0},
        {"KGZ","Kyrgyzstan",10.0},
        {"KHM","Cambodia",20.0},
        {"KNA","Saint Kitts and Nevis",33.0},
        {"KOR","Republic of Korea",26.5},
        {"KWT","Kuwait",15.0},
        {"LAO","Lao People's Democratic Republic",20.0},
        {"LBN","Lebanon",17.0},
        {"LBR","Liberia",25.0},
        {"LBY","Libya",20.0},
        {"LCA","Saint Lucia",30.0},
        {"LKA","Sri Lanka",30.0},
        {"LSO","Lesotho",25.0},
        {"LTU","Lithuania",15.0},
        {"LUX","Luxembourg",24.94},
        {"LVA","Latvia",20.0},
        {"MAC","China, Macao Special Administrative Region",12.0},
        {"MAR","Morocco",32.0},
        {"MDA","Republic of Moldova",12.0},
        {"MDG","Madagascar",20.0},
        {"MDV","Maldives",15.0},
        {"MEX","Mexico",30.0},
        {"MKD","The former Yugoslav Republic of Macedonia",10.0},
        {"MLI","Mali",30.0},
        {"MLT","Malta",35.0},
        {"MMR","Myanmar",22.0},
        {"MNG","Mongolia",25.0},
        {"MOZ","Mozambique",32.0},
        {"MRT","Mauritania",25.0},
        {"MUS","Mauritius",15.0},
        {"MWI","Malawi",30.0},
        {"MYS","Malaysia",24.0},
        {"NAM","Namibia",32.0},
        {"NER","Niger",30.0},
        {"NGA","Nigeria",30.0},
        {"NIC","Nicaragua",30.0},
        {"NLD","Netherlands",25.8},
        {"NOR","Norway",22.0},
        {"NPL","Nepal",25.0},
        {"NZL","New Zealand",28.0},
        {"OMN","Oman",15.0},
        {"PAK","Pakistan",29.0},
        {"PAN","Panama",25.0},
        {"PER","Peru",29.5},
        {"PHL","Philippines",25.0},
        {"PNG","Papua New Guinea",30.0},
        {"POL","Poland",19.0},
        {"PRI","Puerto Rico",37.5},
        {"PRT","Portugal",31.5},
        {"PRY","Paraguay",10.0},
        {"QAT","Qatar",10.0},
        {"ROU","Romania",16.0},
        {"RUS","Russian Federation",20.0},
        {"RWA","Rwanda",30.0},
        {"SAU","Saudi Arabia",20.0},
        {"SDN","Sudan",35.0},
        {"SEN","Senegal",30.0},
        {"SGP","Singapore",17.0},
        {"SLB","Solomon Islands",30.0},
        {"SLE","Sierra Leone",25.0},
        {"SLV","El Salvador",30.0},
        {"SRB","Serbia",15.0},
        {"STP","Sao Tome and Principe",25.0},
        {"SUR","Suriname",36.0},
        {"SVK","Slovakia",21.0},
        {"SVN","Slovenia",19.0},
        {"SWE","Sweden",20.6},
        {"SWZ","Swaziland",27.5},
        {"SYC","Seychelles",25.0},
        {"SYR","Syrian Arab Republic",28.0},
        {"TCD","Chad",35.0},
        {"TGO","Togo",27.0},
        {"THA","Thailand",20.0},
        {"TJK","Tajikistan",18.0},
        {"TKM","Turkmenistan",8.0},
        {"TON","Tonga",25.0},
        {"TTO","Trinidad and Tobago",30.0},
        {"TUN","Tunisia",15.0},
        {"TUR","Turkey",25.0},
        {"TWN","Taiwan",20.0},
        {"TZA","United Republic of Tanzania",30.0},
        {"UGA","Uganda",30.0},
        {"UKR","Ukraine",18.0},
        {"URY","Uruguay",25.0},
        {"USA","United States of America",25.768},
        {"UZB","Uzbekistan",15.0},
        {"VCT","Saint Vincent and the Grenadines",28.0},
        {"VEN","Venezuela (Bolivarian Republic of)",34.0},
        {"VNM","Viet Nam",20.0},
        {"VUT","Vanuatu",0.0},
        {"WSM","Samoa",27.0},
        {"YEM","Yemen",20.0},
        {"ZAF","South Africa",27.0},
        {"ZMB","Zambia",30.0},
        {"ZWE","Zimbabwe",24.72}
    };

    private static final double WORLD_AVERAGE_CORPORATE_TAX_RATE = 23.45;


    // values of Q3 2024
    public static final Object[][] COUNTRY_BASE_RATES = {
        {"CZE", 4.50},   // Czech Republic
        {"DNK", 3.10},   // Denmark
        {"DOM", 6.75},   // Dominican Republic
        {"EGY", 27.25},  // Egypt
        {"SWZ", 7.50},   // Eswatini
        {"FJI", 0.25},   // Fiji
        {"GMB", 17.00},  // Gambia
        {"GEO", 8.00},   // Georgia
        {"GHA", 29.00},  // Ghana
        {"GTM", 5.00},   // Guatemala
        {"HND", 3.00},   // Honduras
        {"HKG", 5.75},   // Hong Kong
        {"HUN", 7.00},   // Hungary
        {"ISL", 9.25},   // Iceland
        {"IND", 6.50},   // India
        {"IDN", 6.25},   // Indonesia
        {"IRN", 23.00},  // Iran
        {"ISR", 4.50},   // Israel
        {"JPN", 0.25},   // Japan
        {"JOR", 7.50},   // Jordan
        {"KAZ", 14.25},  // Kazakhstan
        {"KEN", 13.00},  // Kenya
        {"KWT", 4.25},   // Kuwait
        {"KGZ", 9.00},   // Kyrgyzstan
        {"LBN", 20.00},  // Lebanon
        {"MWI", 26.00},  // Malawi
        {"MYS", 3.00},   // Malaysia
        {"MEX", 10.75},  // Mexico
        {"MDA", 3.60},   // Moldova
        {"MNG", 11.00},  // Mongolia
        {"MAR", 2.75},   // Morocco
        {"MOZ", 14.25},  // Mozambique
        {"NAM", 7.50},   // Namibia
        {"NZL", 5.25},   // New Zealand
        {"NIC", 7.00},   // Nicaragua
        {"NGA", 26.75},  // Nigeria
        {"MKD", 6.30},   // North Macedonia
        {"NLD", 3.65},   // Eurozone (Netherlands as an example)
        {"ZAF", 8.25},   // South Africa
        {"THA", 2.25},   // Thailand
        {"TUR", 30.00},  // Turkey
        {"UKR", 25.00},  // Ukraine
        {"GBR", 5.25},   // United Kingdom
        {"USA", 5.50},   // United States
        {"VEN", 58.12},  // Venezuela
        {"ZMB", 9.00},   // Zambia
        {"ZWE", 150.00}, // Zimbabwe
    };

    private static final double WORLD_AVERAGE_BASE_RATE = 0.1411;     // 14.11%
    private static final double DEFAULT_MARKET_RETURN_RATE = 0.2493;  // 24.93%
}
