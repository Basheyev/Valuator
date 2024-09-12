package com.axiom.valuator.services;

import com.axiom.valuator.data.CompanyData;
import com.axiom.valuator.math.FinancialMath;

import java.util.Locale;

public class ValuatorService {

    public static final int HISTORICAL_DATA_YEARS = 5;
    public static final int DEFAULT_GROWTH_EBITDA_MULTIPLE = 4;
    public static final int FAST_GROWTH_EBITDA_MULTIPLE = 6;


    public static double valuateDCF(CompanyData company, String isoAlpha2Code, StringBuilder report) {
        boolean logReport = report != null;
        Locale country = CountryDataService.getCountryByCode(isoAlpha2Code);

        CountryDataService countryData = new CountryDataService(isoAlpha2Code, HISTORICAL_DATA_YEARS);


        double[] fcf = company.getFreeCashFlow();

        double cash = company.getCash();
        double equity = company.getEquity();
        double equityRate = company.getEquityRate();
        double debt = company.getDebt();
        double debtRate = company.getDebtRate();
        double corporateTax = countryData.getCorporateTax();
        double growthRate = countryData.getAverageGDPGrowthRate();
        double baseRate = countryData.getRiskFreeRate();
        double marketReturn = countryData.getMarketReturn();

        if (logReport) {
            report.append("\n--------------------------------------------\n");
            report.append("Discounted Cash Flow (FCF) Valuation\n");
            report.append("--------------------------------------------\n");
        }

        double WACC = FinancialMath.getWACC(debt, debtRate, equity, equityRate, corporateTax);
        if (WACC==0.0) {
            WACC = FinancialMath.getCAPM(baseRate, 1, marketReturn);
            if (logReport) report.append("CAPM = ").append(Math.round(WACC*10000.0)/100.0).append("%\n");
        } else if (logReport) report.append("WACC = " ).append(Math.round(WACC*10000.0)/100.0).append("%\n");

        if (logReport) report.append("Growth = ").append(Math.round(growthRate * 10000) / 100.0).append("%\n");

        double DCF = FinancialMath.getDCF(fcf, WACC);
        if (logReport) report.append("DCF = ").append(countryData.formatMoney(DCF)).append("\n");

        double TV = FinancialMath.getTerminalValue(fcf[fcf.length-1], WACC, growthRate);
        if (logReport) report.append("TV = ").append(countryData.formatMoney(TV)).append("\n");

        double NFP = debt - cash;
        if (logReport) report.append("NFP = ").append(countryData.formatMoney(NFP)).append("\n");

        double equityValue = DCF + TV - NFP;
        if (logReport) {
            report.append("Valuation = ").append(countryData.formatMoney(equityValue)).append("\n");
            report.append("--------------------------------------------\n");
        }

        return equityValue;
    }


    public static double valuateMultiples(CompanyData company, String listedCompanyTicker, StringBuilder report) {
        try {
            StockDataService sds = new StockDataService(listedCompanyTicker);
            double EVtoRevenue = sds.getEVToRevenue();
            double EVtoEBITDA = sds.getEVToEBITDA();
            double[] revenue = company.getRevenue();
            double[] ebitda = company.getEBITDA();
            if (revenue != null) return revenue[0] * EVtoRevenue;
            if (ebitda != null) return ebitda[0] * EVtoEBITDA;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


    public static double valuateEBITDA(CompanyData companyData, StringBuilder report) {
        double[] ebitda = companyData.getEBITDA();
        if (ebitda==null || ebitda.length == 0) return 0;
        double beginningValue = ebitda[0];
        double endingValue = ebitda[ebitda.length-1];
        int periods = ebitda.length-1;
        double CAGR = FinancialMath.getCAGR(beginningValue, endingValue, periods);
        if (CAGR >= 0.5)
            return beginningValue * FAST_GROWTH_EBITDA_MULTIPLE;
        else
            return beginningValue * DEFAULT_GROWTH_EBITDA_MULTIPLE;
    }



}
