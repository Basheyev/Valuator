package com.axiom.valuator.services;

import com.axiom.valuator.data.CompanyData;
import com.axiom.valuator.math.FinancialMath;

import java.time.Year;

public class ValuatorService {

    public static final int HISTORICAL_DATA_YEARS = 5;
    public static final int DEFAULT_GROWTH_MULTIPLE = 4;
    public static final int FAST_GROWTH_MULTIPLE = 6;
    public static final int LEADER_GROWTH_MULTIPLE = 8;

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


    // fixme wrong for negative ebitda
    public double valuateMultiples(StringBuilder report) {
        try {
            boolean logReport = report != null;
            String listedCompanyTicker = company.getComparableStock();
            StockDataService sds = new StockDataService(listedCompanyTicker);
            double EVtoRevenue = sds.getEVToRevenue();
            double EVtoEBITDA = sds.getEVToEBITDA();
            double[] revenue = company.getRevenue();
            double[] ebitda = company.getEBITDA();
            double EVRvaluation = (revenue != null) ? revenue[0] * EVtoRevenue : 0;
            double EVEvaluation = (ebitda != null && ebitda[0] > 0) ? ebitda[0] * EVtoEBITDA : 0;
            int count = ((revenue != null) ? 1 :0) + ((ebitda != null && ebitda[0] > 0) ? 1 :0);
            double valuation =  (EVRvaluation + EVEvaluation) / count;

            if (logReport) {
                report.append("\n--------------------------------------------\n");
                report.append(company.getName());
                report.append(" Multiples Valuation\n");
                report.append("--------------------------------------------\n");
                report.append(sds).append("\n");
                report.append(company.getName());
                report.append(" valuation:\n");
                report.append("EV/Revenue: ").append(countryData.formatMoney(EVRvaluation)).append("\n");
                report.append("EV/EBITDA: ").append(countryData.formatMoney(EVEvaluation)).append("\n");
                report.append("EV average: ").append(countryData.formatMoney(valuation)).append("\n");
            }
            return EVRvaluation;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // fixme wrong for negative ebitda
    public double valuateEBITDA(StringBuilder report) {
        boolean logReport = report != null;

        double[] ebitda = company.getEBITDA();
        if (ebitda==null || ebitda.length == 0) return 0;
        double beginningValue = ebitda[0];
        double endingValue = ebitda[ebitda.length-1];
        int periods = ebitda.length-1;
        double CAGR = FinancialMath.getCAGR(beginningValue, endingValue, periods);
        double multiple = (CAGR >= 0.5) ? FAST_GROWTH_MULTIPLE : DEFAULT_GROWTH_MULTIPLE;
        if (company.isLeader()) multiple = LEADER_GROWTH_MULTIPLE;

        double baseEBITDA = beginningValue;
        int currentYear = Year.now().getValue();
        // find first positive ebitda
        for (int i=0; i<ebitda.length; i++) {
            double value = ebitda[i];
            int year = company.getDataFirstYear() + i;
            if (value > 0 && year >= currentYear) {
                baseEBITDA = value;
                break;
            }
        }

        double enterpriseValue = baseEBITDA * multiple;
        double equityValuation = enterpriseValue - company.getDebt();

        if (logReport) {
            report.append("\n--------------------------------------------\n");
            report.append(company.getName());
            report.append(" EBITDA Multiple Valuation\n");
            report.append("--------------------------------------------\n");
            report.append("Growth rate: ").append(Math.round(CAGR*10000.0)/100.0).append("%\n");
            report.append("Multiple: ").append(multiple).append("x\n");
            report.append("Valuation: ").append(countryData.formatMoney(equityValuation)).append("\n");
        }

        return equityValuation;
    }



}
