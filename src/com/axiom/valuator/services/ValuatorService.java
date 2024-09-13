package com.axiom.valuator.services;

import com.axiom.valuator.data.CompanyData;
import com.axiom.valuator.math.FinancialMath;

public class ValuatorService {

    public static final int HISTORICAL_DATA_YEARS = 5;
    public static final int DEFAULT_GROWTH_MULTIPLE = 4;
    public static final int FAST_GROWTH_MULTIPLE = 6;

    private CountryDataService countryData;
    private CompanyData company;

    public ValuatorService(CompanyData companyData) {
        company = companyData;
        countryData = new CountryDataService(company.getCountry(), HISTORICAL_DATA_YEARS);
    }

    public double valuateDCF(StringBuilder report) {
        boolean logReport = report != null;

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

        double WACC = FinancialMath.getWACC(debt, debtRate, equity, equityRate, corporateTax);
        if (WACC==0.0) // todo calculate beta
            WACC = FinancialMath.getCAPM(baseRate, 1, marketReturn);
        double DCF = FinancialMath.getDCF(fcf, WACC);
        double TV = FinancialMath.getTerminalValue(fcf[fcf.length-1], WACC, growthRate);
        double NFP = debt - cash;
        double equityValue = DCF + TV - NFP;

        if (logReport) {
            report.append("\n--------------------------------------------\n");
            report.append(company.getName());
            report.append(" Discounted Cash Flow (FCF) Valuation\n");
            report.append("--------------------------------------------\n");
            report.append("WACC = ").append(Math.round(WACC*10000.0)/100.0).append("%\n");
            report.append("Growth = ").append(Math.round(growthRate * 10000) / 100.0).append("%\n");
            report.append("DCF = ").append(countryData.formatMoney(DCF)).append("\n");
            report.append("TV = ").append(countryData.formatMoney(TV)).append("\n");
            report.append("NFP = ").append(countryData.formatMoney(NFP)).append("\n");
            report.append("Valuation = ").append(countryData.formatMoney(equityValue)).append("\n");
            //report.append("--------------------------------------------\n");
        }

        return equityValue;
    }


    public double valuateMultiples(String listedCompanyTicker, StringBuilder report) {
        try {
            boolean logReport = report != null;
            StockDataService sds = new StockDataService(listedCompanyTicker);
            System.out.println(sds);
            double EVtoRevenue = sds.getEVToRevenue();
            double EVtoEBITDA = sds.getEVToEBITDA();
            double[] revenue = company.getRevenue();
            double[] ebitda = company.getEBITDA();
            double valuation = 0;
            if (revenue != null) valuation = revenue[0] * EVtoRevenue;
            if (ebitda != null) valuation = ebitda[0] * EVtoEBITDA;
            if (logReport) {
                report.append("\n--------------------------------------------\n");
                report.append(company.getName());
                report.append(" Multiples Valuation\n");
                report.append("--------------------------------------------\n");
                report.append(sds);
                report.append(company.getName());
                report.append(" valuation: ");
                report.append(countryData.formatMoney(valuation));
            }
            return valuation;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


    public double valuateEBITDA(StringBuilder report) {
        boolean logReport = report != null;

        double[] ebitda = company.getEBITDA();
        if (ebitda==null || ebitda.length == 0) return 0;
        double beginningValue = ebitda[0];
        double endingValue = ebitda[ebitda.length-1];
        int periods = ebitda.length-1;
        double CAGR = FinancialMath.getCAGR(beginningValue, endingValue, periods);
        double multiple = (CAGR >= 0.5) ? FAST_GROWTH_MULTIPLE : DEFAULT_GROWTH_MULTIPLE;
        double valuation = beginningValue * multiple;

        if (logReport) {
            report.append("\n--------------------------------------------\n");
            report.append(company.getName());
            report.append(" EBITDA Multiple Valuation\n");
            report.append("--------------------------------------------\n");
            report.append("Growth rate: ").append(Math.round(CAGR*10000.0)/100.0).append("\n");
            report.append("Multiple: ").append(multiple).append("x\n");
            report.append("Valuation: ").append(countryData.formatMoney(valuation)).append("\n");
        }

        return valuation;
    }



}
