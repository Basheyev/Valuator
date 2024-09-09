package com.axiom.valuator.methods;

import com.axiom.valuator.data.CompanyData;
import com.axiom.valuator.math.FinancialMath;
import com.axiom.valuator.services.CountryDataService;

public class DCFValuator {

    public static final int HISTORICAL_DATA_YEARS = 5;
    private CountryDataService countryData;

    public DCFValuator(String isoAlpha2Code) {
        countryData = new CountryDataService(isoAlpha2Code, HISTORICAL_DATA_YEARS);
    }


    public double valuate(CompanyData company) {
        double[] fcf = company.getFreeCashFlow();
        double cash = company.getCash();
        double equity = company.getEquity();
        double equityRate = company.getEquityRate();
        double debt = company.getDebt();
        double debtRate = company.getDebtRate();
        return valuate(fcf, cash, equity, equityRate, debt, debtRate);
    }


    private double valuate(double[] fcf, double cash, double equity, double equityRate, double debt, double debtRate) {
        double corporateTax = countryData.getCorporateTax();
        double growthRate = countryData.getAverageGDPGrowthRate();
        double baseRate = countryData.getRiskFreeRate();     // 14.25%
        double marketReturn = 0.2493; // 24.93%
        double WACC = FinancialMath.getWACC(debt, debtRate, equity, equityRate, corporateTax);
        if (WACC==0.0) WACC = FinancialMath.getCAPM(baseRate, 1, marketReturn);
        double DCF = FinancialMath.getDCF(fcf, WACC);
        double TV = FinancialMath.getTerminalValue(fcf[fcf.length-1], WACC, growthRate);
        double NFP = debt - cash;
        return DCF + TV - NFP;
    }





}
