package com.axiom.valuator.data;

import java.util.Locale;

public class CompanyData {

    private final String name;
    private double[] fcf;
    private double cash;
    private double equity;
    private double equityRate;
    private double debt;
    private double debtRate;

    public CompanyData(String name) {
        this.name = name;
    }

    public void setFreeCashFlow(double[] revenue) {
        fcf = revenue;
    }

    public double[] getFreeCashFlow() {
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

    public void setEquity(double equity) {
        this.equity = equity;
    }

    public double getEquityRate() {
        return equityRate;
    }

    public void setEquityRate(double equityRate) {
        this.equityRate = equityRate;
    }

    public double getDebt() {
        return debt;
    }

    public void setDebt(double debt) {
        this.debt = debt;
    }

    public double getDebtRate() {
        return debtRate;
    }

    public void setDebtRate(double debtRate) {
        this.debtRate = debtRate;
    }



}
