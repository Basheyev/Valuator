package com.axiom.valuator.data;


import com.axiom.valuator.math.FinancialMath;
import com.axiom.valuator.services.CountryDataService;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Company Financial Data
 * Represents a data structure used to store and manage key financial information
 * related to a company's valuation. It includes attributes for cash flows, equity,
 * debts and their interest rates, as well as other financial metrics necessary
 * for the valuation process.
 */
public class CompanyData {

    private final String name;
    private Locale country;
    private double[] revenue;
    private double[] ebitda;
    private double[] fcf;
    private double cash;
    private double equity;
    private double equityRate;
    private double debt;
    private double debtRate;

    public CompanyData(String name, String countryISO2alphaCode) {
        this.name = name;
        this.country = CountryDataService.getCountryByCode(countryISO2alphaCode);
        this.revenue = null;
        this.ebitda = null;
        this.fcf = null;
        this.cash = 0;
        this.equity = 0;
        this.equityRate = 0;
        this.debt = 0;
        this.debtRate = 0;
    }

    public String getName() {
        return name;
    }

    public double[] getRevenue() {
        return revenue;
    }

    public void setRevenue(double[] revenue) {
        this.revenue = revenue;
    }

    public double[] getEBITDA() {
        return ebitda;
    }

    public void setEBITDA(double[] ebitda) {
        this.ebitda = ebitda;
    }

    public void setFCF(double[] cashFlow) {
        fcf = cashFlow;
    }

    public double[] getFCF() {
        return fcf;
    }


    public double getCash() {
        return cash;
    }

    public void setCash(double cash) {
        this.cash = cash;
    }

    public double getEquity() {
        return equity;
    }

    public void setEquity(double equity, double equityRate) {
        this.equity = equity;
        this.equityRate = equityRate;
    }

    public double getEquityRate() {
        return equityRate;
    }

    public double getDebt() {
        return debt;
    }

    public void setDebt(double debt, double debtRate) {
        this.debt = debt;
        this.debtRate = debtRate;
    }

    public double getDebtRate() {
        return debtRate;
    }


    private String generateStringOfValues(String header, double[] values) {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(country);
        currencyFormatter.setMaximumFractionDigits(0);
        StringBuilder sb = new StringBuilder();
        sb.append(header);
        for (int i = 0; i < values.length; i++) {
            sb.append("\t\t");
            sb.append(currencyFormatter.format(values[i]));
            //sb.append(" (Y").append(i).append(")");
        }
        sb.append("\n");
        return sb.toString();
    }


    @Override
    public String toString() {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(country);
        currencyFormatter.setMaximumFractionDigits(0);
        StringBuilder sb = new StringBuilder();
        sb.append("-------------------------------------------------------------------------------------\n")
          .append(name).append(" (").append(country.getCountry()).append(")\n")
          .append("-------------------------------------------------------------------------------------\n");
        if (revenue != null) sb.append(generateStringOfValues("Revenue", revenue));
        if (ebitda != null) sb.append(generateStringOfValues("EBITDA", ebitda));
        if (fcf != null) sb.append(generateStringOfValues("Free CF", fcf));
        sb.append("-------------------------------------------------------------------------------------\n");
        double eRate = Math.round(equityRate * 100);
        sb.append("Equity:\t")
          .append(currencyFormatter.format(equity))
          .append(" (rate ").append(eRate).append("%)\n");
        double dRate = Math.round(debtRate * 100);
        sb.append("Debt:\t")
                .append(currencyFormatter.format(debt))
                .append(" (rate ").append(dRate).append("%)\n");
        sb.append("Cash:\t")
                .append(currencyFormatter.format(cash));
        sb.append("\n-------------------------------------------------------------------------------------\n");
        return sb.toString();
    }
}
