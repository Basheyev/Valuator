package com.axiom.valuator.methods;

import com.axiom.valuator.data.CompanyData;
import com.axiom.valuator.math.FinancialMath;
import com.axiom.valuator.services.CountryDataService;
import com.axiom.valuator.services.StockDataService;

public class Valuator {

    public static final int HISTORICAL_DATA_YEARS = 5;
    public static final int DEFAULT_GROWTH_EBITDA_MULTIPLE = 4;
    public static final int FAST_GROWTH_EBITDA_MULTIPLE = 6;


    public static double valuateDCF(CompanyData company, String isoAlpha2Code) {
        CountryDataService countryData = new CountryDataService(isoAlpha2Code, HISTORICAL_DATA_YEARS);
        double[] fcf = company.getFCF();
        double cash = company.getCash();
        double equity = company.getEquity();
        double equityRate = company.getEquityRate();
        double debt = company.getDebt();
        double debtRate = company.getDebtRate();
        double corporateTax = countryData.getCorporateTax();
        double growthRate = countryData.getAverageGDPGrowthRate();
        double baseRate = countryData.getRiskFreeRate();
        double marketReturn = countryData.getMarketReturn();
        double WACC = FinancialMath.getWACC(debt, debtRate, equity, equityRate, corporateTax);
        if (WACC==0.0) WACC = FinancialMath.getCAPM(baseRate, 1, marketReturn);
        double DCF = FinancialMath.getDCF(fcf, WACC);
        double TV = FinancialMath.getTerminalValue(fcf[fcf.length-1], WACC, growthRate);
        double NFP = debt - cash;
        return DCF + TV - NFP;
    }


    public static double valuateMultiples(CompanyData company, String listedCompanyTicker) {
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


    public static double valuateEBITDA(CompanyData companyData) {
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
