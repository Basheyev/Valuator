package com.axiom.valuator.data;

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

import static com.axiom.valuator.math.FinancialMath.toPercent;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.http.HttpClient.newHttpClient;

/**
 * Fetches and stores country GDP values by years, GDP growth (%),
 * inflation (%), corporate tax (%) and base rate (%) that are
 * used in WACC, CAPM and Terminal Value formulas calculations
 */
public class CountryData {

    //-----------------------------------------------------------------------------------------------------
    public static final String WB_URL = "https://api.worldbank.org/v2/country/{CODE}/indicator/";
    public static final String WB_REAL_GDP = "NY.GDP.MKTP.KD";      // GDP Indicator
    public static final String WB_INFLATION = "NY.GDP.DEFL.KD.ZG";  // Inflation Indicator
    public static final String WB_YEARS_RANGE = "?date=";           // Years range argument
    public static final String WB_FORMAT = "&format=json";          // Data format argument
    public static final String WB_DATE_FIELD = "date";              // JSON value field name
    public static final String WB_VALUE_FIELD = "value";            // JSON value field name
    public static final int WB_RESPONSE_VALUES_INDEX = 1;           // Index of values in response
    public static final int MINIMUM_YEARS_OF_HISTORY = 3;           // Minimum years of history
    public static final int DEFAULT_YEARS_OF_HISTORY = 5;           // Default years of history
    public static final int MAXIMUM_YEARS_OF_HISTORY = 10;          // Maximum years of history

    //-----------------------------------------------------------------------------------------------------
    public static final String ERROR_WRONG_URL = "Can't fetch data from ";
    public static final String ERROR_WRONG_LOCALE = "Locale can not be null";

    //-----------------------------------------------------------------------------------------------------
    private final String WB_API;                      // Tailored World Bank API URL for specific country
    private final Locale country;                     // Country, currency and language
    private final int yearsOfHistory;                 // How many years of GDP & Inflation values to store
    private final int firstYear;                      // First year of GDP and inflation values
    private final int lastYear;                       // last year of GDP and inflation values
    private final double[] gdpValues;                 // Array of GDP values
    private final double[] inflationValues;           // Array of Inflation values
    private final double averageGDPGrowthRate;        // Average GDP growth rate
    private final double averageInflationRate;        // Average Inflation rate
    private final double corporateTax;                // Corporate Tax Level
    private final double interestRate;                // Central Bank Interest Rate
    private final double marketReturnRate;            // Average Market Return Rate
    private final NumberFormat currencyFormatter;     // Currency formatter
    //-----------------------------------------------------------------------------------------------------


    /**
     * Constructor that loads economic data about the country
     * (GDP, Inflation, Corporate Tax, Base Interest Rate) from open data sources
     * @param countryLocale country
     * @param howManyYears how many years of history to load
     */
    public CountryData(Locale countryLocale, int howManyYears) {
        // Check constructor arguments
        if (countryLocale == null) throw new IllegalArgumentException(ERROR_WRONG_LOCALE);
        if (howManyYears < MINIMUM_YEARS_OF_HISTORY) howManyYears = MINIMUM_YEARS_OF_HISTORY;
        else if (howManyYears > MAXIMUM_YEARS_OF_HISTORY) howManyYears = MAXIMUM_YEARS_OF_HISTORY;
        // Initialize constants
        country = countryLocale;
        yearsOfHistory = howManyYears;
        WB_API = WB_URL.replace("{CODE}", country.getCountry());
        lastYear = Year.now().getValue() - 1;
        firstYear = lastYear - (yearsOfHistory - 1);
        gdpValues = new double[yearsOfHistory];
        inflationValues = new double[yearsOfHistory];
        averageGDPGrowthRate = fetchRealGDPData(firstYear, lastYear, gdpValues);
        averageInflationRate = fetchInflationData(firstYear, lastYear, inflationValues);
        corporateTax = fetchCorporateTaxRate(country);
        interestRate = fetchCentralBankInterestRate(country);
        marketReturnRate = fetchMarketReturnRate(country);
        // initialize currency formatter
        currencyFormatter = NumberFormat.getCurrencyInstance(country);
        currencyFormatter.setMaximumFractionDigits(0);
    }


    /**
     * Default constructor
     * @param countryLocale country
     */
    public CountryData(Locale countryLocale) {
        this(countryLocale, DEFAULT_YEARS_OF_HISTORY);
    }


    /**
     * Fetches GDP data from World Bank for YEARS_OF_HISTORY period
     * @param firstYear first year to fetch data
     * @param lastYear last year to fetch data
     * @param values output array to save inflation values
     * @return compound average GDP growth rate
     */
    private double fetchRealGDPData(int firstYear, int lastYear, double[] values) {
        // Build request URL
        String url = WB_API + WB_REAL_GDP + WB_YEARS_RANGE + firstYear + ":" + lastYear + WB_FORMAT;
        // Send GET request
        String jsonResponse = sendRequest(url);
        if (jsonResponse==null) throw new IllegalArgumentException(ERROR_WRONG_URL + url);
        // Parse JSON Object
        JSONArray response = new JSONArray(jsonResponse);
        JSONArray jsonArray = (JSONArray) response.get(WB_RESPONSE_VALUES_INDEX);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject gdpEntry = jsonArray.getJSONObject(i);
            int year = Integer.parseInt(gdpEntry.getString(WB_DATE_FIELD));
            int index = year - firstYear;
            double value = gdpEntry.isNull(WB_VALUE_FIELD) ? 0.0d : gdpEntry.getDouble(WB_VALUE_FIELD);
            values[index] = value;
        }
        int periods = values.length - 1;
        double startValue = values[0];
        double endValue = values[periods];

        return FinancialMath.getCAGR(startValue, endValue, periods);
    }


    /**
     * Fetches inflation data from World Bank for YEARS_OF_HISTORY period
     * @param firstYear first year to fetch data
     * @param lastYear last year to fetch data
     * @param values output array to save inflation values
     * @return arithmetic average inflation
     */
    private double fetchInflationData(int firstYear, int lastYear, double[] values) {
        // Build request URL
        String url = WB_API + WB_INFLATION + WB_YEARS_RANGE + firstYear + ":" + lastYear + WB_FORMAT;
        // Send GET request
        String jsonResponse = sendRequest(url);
        if (jsonResponse==null) throw new IllegalArgumentException(ERROR_WRONG_URL + url);
        // Parse JSON Object
        JSONArray response = new JSONArray(jsonResponse);
        JSONArray jsonArray = (JSONArray) response.get(1);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject gdpEntry = jsonArray.getJSONObject(i);
            int year = Integer.parseInt(gdpEntry.getString(WB_DATE_FIELD));
            int index = year - firstYear;
            double value = gdpEntry.isNull(WB_VALUE_FIELD) ? 0.0d : gdpEntry.getDouble(WB_VALUE_FIELD);
            values[index] = value / 100.0;
        }
        return getValuesAverage(values);
    }


    /**
     * Calculates doubles array arithmetic average value
     * @param values array of double values
     * @return arithmetic average value
     */
    private double getValuesAverage(double[] values) {
        if (values==null || values.length==0) return 0;
        double sum = 0;
        for (double value : values) {
            if (value == 0) break;
            sum += value;
        }
        return sum / values.length;
    }


    /**
     * Returns Country Corporate Tax Rate
     * @param country country
     * @return country corporate tax rate or average worldwide if unkown country
     */
    private static double fetchCorporateTaxRate(Locale country) {
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


    /**
     * Returns Central Bank Base Interest Rate from static data
     * @param country country
     * @return central bank interest rate or world average if country not found
     */
    private double fetchCentralBankInterestRate(Locale country) {
        String iso3Code = country.getISO3Country();
        for (Object[] tuple: COUNTRIES_BASE_RATES) {
            String iso3 = (String) tuple[0];
            double rate = (Double) tuple[1];
            if (iso3.equals(iso3Code)) {
                return rate / 100.0;
            }
        }
        return WORLD_AVERAGE_BASE_RATE;
    }


    /**
     * Returns Country Average Market Return Rate from static data
     * @param country country
     * @return country average market return rate or world average if country not found
     */
    private double fetchMarketReturnRate(Locale country) {

        // todo get real data on market return

        return DEFAULT_MARKET_RETURN_RATE;
    }


    public String getCountryCode() { return country.getCountry(); }
    public String getCountryName() { return country.getDisplayCountry(); }
    public int getFirstYear() { return firstYear; }
    public int getLastYear() { return lastYear; }
    public double getAverageGDPGrowthRate() { return averageGDPGrowthRate; }
    public double getAverageInflationRate() { return averageInflationRate; }
    public double getCorporateTax() { return corporateTax; }
    public double getRiskFreeRate() { return interestRate; }
    public double getMarketReturn() { return marketReturnRate; }


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
    private String sendRequest(String URL) {
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


    /**
     * Formats double value to locale currency format
     * @param moneyValue money value
     * @return formatted currency string
     */
    public String formatMoney(double moneyValue) {
        return currencyFormatter.format(moneyValue);
    }

    @Override
    public String toString() {
        Locale region = CountryData.getCountryByCode("US");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(region);
        StringBuilder sb = new StringBuilder();
        sb.append(getCountryName()).append(" (").append(country.getCountry()).append(")\n");
        for (int i = 0; i < yearsOfHistory; i++) {
            sb.append(firstYear + i).append(" GDP: ")
              .append(currencyFormatter.format(gdpValues[i])).append(" (");
            if (i>0) {
                double growthYoY = (gdpValues[i] / gdpValues[i-1] - 1.0);
                sb.append("growth ").append(toPercent(growthYoY)).append("%, ");
            }
            sb.append("inflation ").append(toPercent(inflationValues[i])).append("%)\n");
        }
        sb.append("Average GDP growth rate: ").append(toPercent(getAverageGDPGrowthRate())).append("%\n");
        sb.append("Average Inflation Rate: ").append(toPercent(getAverageInflationRate())).append("%\n");
        sb.append("Interest Rate: ").append(toPercent(getRiskFreeRate())).append("%\n");
        sb.append("Corporate Tax Rate: ").append(toPercent(getCorporateTax())).append("%");
        return sb.toString();
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
    public static final Object[][] COUNTRIES_BASE_RATES = {
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
