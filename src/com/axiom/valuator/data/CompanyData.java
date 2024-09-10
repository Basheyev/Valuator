package com.axiom.valuator.data;


/**
 * Company Financial Data
 * Represents a data structure used to store and manage key financial information
 * related to a company's valuation. It includes attributes for cash flows, equity,
 * debts and their interest rates, as well as other financial metrics necessary
 * for the valuation process.
 */
public class CompanyData {

    private final String name;
    private double[] revenue;
    private double[] ebitda;
    private double[] fcf;
    private double cash;
    private double equity;
    private double equityRate;
    private double debt;
    private double debtRate;

    public CompanyData(String name) {
        this.name = name;
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

    public double[] getEbitda() {
        return ebitda;
    }

    public void setEbitda(double[] ebitda) {
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




}
