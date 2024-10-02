//=======================================================================================
//
//  PRIVATE COMPANY VALUATOR
//  Company Data Container
//
// (C) 2024 Axiom Capital, Bolat Basheyev
//=======================================================================================
package com.axiom.valuator.model;

import com.axiom.valuator.math.FinancialMath;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * COMPANY DATA
 * Represents a minimal data structure used to store key financial information
 * related to a company's valuation. It includes attributes for revenue, ebitda,
 * cash flows, equity, debts and their interest rates, as well as other financial
 * metrics necessary for the valuation process.
 */
public class CompanyData {

    private final String name;                  // Company legal entity name
    private final Locale country;               // Company head office location country
    private final int dataFirstYear;            // Data beginning year
    private double[] revenue;                   // Revenue values by periods
    private double[] ebitda;                    // EBITDA values by periods
    private double[] freeCashFlow;              // Free Cash Flow values by periods
    private double equity, equityRate;          // Equity capital and its cost
    private double debt, debtRate;              // Debt capital and its cost
    private double cash;                        // Cash and cash equivalents
    private double marketShare;                 // Market share
    private String comparableStock;             // Comparable stock ticker
    private int ventureExitYear;                // Venture forecasted exit year
    private double ventureRate;                 // Venture interest rate

    /**
     * Company Data Constructor
     * @param name company legal entity name for reporting
     * @param countryCode head office country (ISO Alpha-2 Country Code)
     * @param startYear beginning year of Revenue, EBITDA, Free Cash Flow data
     */
    public CompanyData(String name, String countryCode, int startYear) {
        this.name = name;
        this.country = CountryData.getCountryByCode(countryCode);
        this.dataFirstYear = startYear;
        this.revenue = null;
        this.ebitda = null;
        this.freeCashFlow = null;
        this.cash = 0;
        this.equity = 0;
        this.equityRate = 0;
        this.debt = 0;
        this.debtRate = 0;
        this.marketShare = 0;
        this.comparableStock = "";
        this.ventureExitYear = dataFirstYear;
        this.ventureRate = 0;
    }


    /**
     * Company Data Constructor
     * @param json JSONObject containing company financial data
     */
    public CompanyData(JSONObject json) {
        this.name = json.getString("name");
        this.country = CountryData.getCountryByCode(json.getString("country"));
        this.dataFirstYear = json.getInt("dataFirstYear");
        this.revenue = jsonArrayToDoubleArray(json.getJSONArray("revenue"));
        this.ebitda = jsonArrayToDoubleArray(json.getJSONArray("ebitda"));
        this.freeCashFlow = jsonArrayToDoubleArray(json.getJSONArray("freeCashFlow"));
        this.cash = json.getDouble("cash");
        this.equity = json.getDouble("equity");
        this.equityRate = json.getDouble("equityRate");
        this.debt = json.getDouble("debt");
        this.debtRate = json.getDouble("debtRate");
        this.marketShare = json.getDouble("marketShare");
        this.comparableStock = json.getString("comparableStock");
        this.ventureExitYear = json.getInt("ventureExitYear");
        this.ventureRate = json.getDouble("ventureRate");
    }


    /**
     * Converts JSONArray to double array
     * @param array JSONArray
     * @return double array
     */
    private double[] jsonArrayToDoubleArray(JSONArray array) {
        int length = array.length();
        double[] values = new double[length];
        for (int i=0; i<length; i++)
            values[i] = array.getDouble(i);
        return values;
    }


    /**
     * Sets values for revenue by periods starting from the first year
     * @param revenueValue array of revenue values by periods
     * @return this company data object
     */
    public CompanyData setRevenue(double[] revenueValue) {
        revenue = revenueValue;
        return this;
    }


    /**
     * Sets values for EBITDA by periods starting from the first year
     * @param ebitdaValue array of EBITDA values by periods
     * @return this company data object
     */
    public CompanyData setEBITDA(double[] ebitdaValue) {
        ebitda = ebitdaValue;
        return this;
    }


    /**
     * Sets values for Free Cash Flow by periods starting from the first year
     * @param freeCashFlowValue array of Free Cash Flow values by periods
     * @return this company data object
     */
    public CompanyData setFreeCashFlow(double[] freeCashFlowValue) {
        freeCashFlow = freeCashFlowValue;
        return this;
    }


    /**
     * Sets cash and cash equivalents amount
     * @param cash cash and cash equivalents amount
     * @return this company data object
     */
    public CompanyData setCashAndEquivalents(double cash) {
        this.cash = cash;
        return this;
    }


    /**
     * Sets equity capital amount and its cost (interest Rate)
     * @param equity amount of equity capital
     * @param equityRate equity interest rate
     * @return this company data object
     */
    public CompanyData setEquity(double equity, double equityRate) {
        this.equity = equity;
        this.equityRate = equityRate;
        return this;
    }


    /**
     * Sets debt capital amount and its cost (interest Rate)
     * @param debt amount of debt capital
     * @param debtRate debt interest rate
     * @return this company data object
     */
    public CompanyData setDebt(double debt, double debtRate) {
        this.debt = debt;
        this.debtRate = debtRate;
        return this;
    }

    /**
     * Sets company market share value
     * @param marketShare market share value
     * @return this company data object
     */
    public CompanyData setMarketShare(double marketShare) {
        this.marketShare = marketShare;
        return this;
    }

    /**
     * Sets comparable public company stock ticker/symbol
     * @param ticker symbol of comparable public company
     * @return this company data object
     */
    public CompanyData setComparableStock(String ticker) {
        this.comparableStock = ticker;
        return this;
    }

    public String getName() { return name; }
    public Locale getCountry() { return country; }
    public int getDataFirstYear() { return dataFirstYear; }
    public double[] getRevenue() { return revenue; }
    public double[] getEBITDA() { return ebitda; }
    public double[] getFreeCashFlow() { return freeCashFlow; }
    public double getEquity() { return equity; }
    public double getEquityRate() { return equityRate; }
    public double getDebt() { return debt; }
    public double getDebtRate() { return debtRate; }
    public double getCashAndEquivalents() { return cash; }
    public double getMarketShare() { return marketShare; }
    public String getComparableStock() { return comparableStock; }
    public int getVentureExitYear() { return ventureExitYear; }
    public double getVentureRate() { return ventureRate; }

    /**
     * Serializes this company data object to JSON
     * @return serialized JSONObject
     */
    public JSONObject toJson() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("name", name);
        map.put("country", country.getCountry());
        map.put("dataFirstYear", dataFirstYear);
        map.put("revenue", revenue);
        map.put("ebitda", ebitda);
        map.put("freeCashFlow", freeCashFlow);
        map.put("equity", equity);
        map.put("equityRate", equityRate);
        map.put("debt", debt);
        map.put("debtRate", debtRate);
        map.put("cash", cash);
        map.put("marketShare", marketShare);
        map.put("comparableStock", comparableStock);
        map.put("ventureExitYear", ventureExitYear);
        map.put("ventureRate", ventureRate);
        return new JSONObject(map);
    }

    /**
     * Formats numeric values in the string with header and formatting
     * @param header header string
     * @param values array of money or numeric values
     * @return formatted string
     */
    private String generateStringOfValues(String header, double[] values, boolean money) {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(country);
        currencyFormatter.setMaximumFractionDigits(0);
        StringBuilder sbFormat = new StringBuilder(header + "\t");
        int length = values.length;
        Object[] valuesObj = new String[length];
        for (int i=0; i<length; i++) {
            sbFormat.append("| %20s ");
            if (money)
                valuesObj[i] = currencyFormatter.format(values[i]);
            else valuesObj[i] = Long.toString(Math.round(values[i]));
        }
        sbFormat.append("\n");
        return String.format(sbFormat.toString(), valuesObj);
    }


    /**
     * Generate formatted string containing company data
     * @return formatted string containing company data
     */
    @Override
    public String toString() {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(country);
        currencyFormatter.setMaximumFractionDigits(0);
        StringBuilder sb = new StringBuilder();

        int revenueLen = revenue != null ? revenue.length : 0;         // Revenue array length
        int ebitdaLen = ebitda != null ? ebitda.length : 0;            // EBITDA array length
        int fcfLen = freeCashFlow != null ? freeCashFlow.length : 0;   // FCF array length
        int len = Math.max(Math.max(revenueLen, ebitdaLen), fcfLen);   // Longest array
        double[] years = new double[len];                              // Allocate memory
        for (int i = 0; i < len; i++) years[i] = dataFirstYear + i;    // Generate years array

        sb.append("\n");
        sb.append(name).append("\n");
        sb.append("-------------------------------------------------------------------------------------\n");
        sb.append(generateStringOfValues("Years", years, false));
        sb.append("-------------------------------------------------------------------------------------\n");
        if (revenue != null) sb.append(generateStringOfValues("Revenue", revenue, true));
        if (ebitda != null) sb.append(generateStringOfValues("EBITDA", ebitda, true));
        if (freeCashFlow != null) sb.append(generateStringOfValues("Free CF", freeCashFlow, true));
        sb.append("-------------------------------------------------------------------------------------\n");
        double eRate = FinancialMath.toPercent(equityRate);
        double dRate = FinancialMath.toPercent(debtRate);
        double vRate = FinancialMath.toPercent(ventureRate);
        double market = FinancialMath.toPercent(marketShare);
        sb.append("Equity:\t").append(currencyFormatter.format(equity))
            .append(" (interest ").append(eRate).append("%)\n");
        sb.append("Debt:\t").append(currencyFormatter.format(debt))
            .append(" (interest ").append(dRate).append("%)\n");
        sb.append("Cash:\t").append(currencyFormatter.format(cash)).append("\n");
        sb.append("Comparable Stock: ").append(comparableStock).append("\n");
        sb.append("Market Share: ").append(market).append("%\n");
        sb.append("Venture Exit Year: ").append(ventureExitYear).append("\n");
        sb.append("Venture Interest Rate: ").append(vRate).append("\n");
       // sb.append("\n-------------------------------------------------------------------------------------\n");
        return sb.toString();
    }

    /**
     * Generate HTML string containing company data
     * @return HTML string containing company data
     */
    public String toHTML() {
        StringBuilder sb = new StringBuilder();

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(country);
        currencyFormatter.setMaximumFractionDigits(0);

        int revenueLen = revenue != null ? revenue.length : 0;         // Revenue array length
        int ebitdaLen = ebitda != null ? ebitda.length : 0;            // EBITDA array length
        int fcfLen = freeCashFlow != null ? freeCashFlow.length : 0;   // FCF array length
        int len = Math.max(Math.max(revenueLen, ebitdaLen), fcfLen);   // Longest array
        int[] years = new int[len];                                    // Allocate memory
        for (int i = 0; i < len; i++) years[i] = dataFirstYear + i;    // Generate years array

        //----------------------------------------------------------------------
        sb.append("<h5>").append(name).append(" (").append(country.getCountry()).append(")</h5>");
        sb.append("<table class=\"table table-bordered custom-header\">");

        sb.append("<tr>");
        sb.append("<th class=\"text-start\">").append("</th>");
        for (int year:years) sb.append("<th class=\"text-end\">").append(year).append("</th>");
        sb.append("</tr>");

        sb.append("<tr>").append("<td class=\"text-start\">Revenue</td>");
        for (int i=0; i<len; i++) {
            sb.append("<td class=\"text-end\">");
            sb.append((i<revenueLen) ? currencyFormatter.format(revenue[i]) : 0);
            sb.append("</td>");
        }
        sb.append("<tr>");

        sb.append("<tr>").append("<td class=\"text-start\">EBITDA</td>");
        for (int i=0; i<len; i++) {
            sb.append("<td class=\"text-end\">");
            sb.append((i<ebitdaLen) ? currencyFormatter.format(ebitda[i]) : 0);
            sb.append("</td>");
        }
        sb.append("<tr>");

        sb.append("<tr>").append("<td class=\"text-start\">Cash Flow</td>");
        for (int i=0; i<len; i++) {
            sb.append("<td class=\"text-end\">");
            sb.append((i<fcfLen) ? currencyFormatter.format(freeCashFlow[i]) : 0);
            sb.append("</td>");
        }
        sb.append("<tr>");

        sb.append("</table>");

        //----------------------------------------------------------------------
        sb.append("<table class=\"table table-bordered text-center\">");

        sb.append("<tr style=\"height: 30px;\">");
        sb.append("<td class=\"text-start\">").append("Equity Invested").append("</td>");
        sb.append("<td class=\"text-end\">").append(currencyFormatter.format(equity)).append("</td>");
        sb.append("<td class=\"text-start\">").append("Equity Rate").append("</td>");
        sb.append("<td class=\"text-end\">").append(FinancialMath.toPercent(equityRate)).append("%</td>");
        sb.append("</tr>");

        sb.append("<tr>");
        sb.append("<td class=\"text-start\">").append("Debt Borrowed").append("</td>");
        sb.append("<td class=\"text-end\">").append(currencyFormatter.format(debt)).append("</td>");
        sb.append("<td class=\"text-start\">").append("Debt Rate").append("</td>");
        sb.append("<td class=\"text-end\">").append(FinancialMath.toPercent(debtRate)).append("%</td>");
        sb.append("</tr>");

        sb.append("<tr>");
        sb.append("<td class=\"text-start\">").append("Cash & Equivalents").append("</td>");
        sb.append("<td class=\"text-end\">").append(currencyFormatter.format(cash)).append("</td>");
        sb.append("<td class=\"text-start\">").append("Market Share").append("</td>");
        sb.append("<td class=\"text-end\">").append(FinancialMath.toPercent(marketShare)).append("%</td>");
        sb.append("</tr>");

        sb.append("<tr>");

        sb.append("<td class=\"text-start\">").append("Comparable").append("</td>");
        sb.append("<td class=\"text-end\">").append(comparableStock).append("</td>");
        sb.append("<td class=\"text-start\">").append("Venture Rate").append("</td>");
        sb.append("<td class=\"text-end\">").append(FinancialMath.toPercent(ventureRate)).append("%</td>");
        sb.append("</tr>");

        sb.append("</table>");
        //----------------------------------------------------------------------

        return sb.toString();
    }


}
