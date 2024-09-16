package com.axiom.valuator.services;

import com.axiom.valuator.data.CompanyData;
import com.axiom.valuator.data.CountryData;
import com.axiom.valuator.data.StockData;
import com.axiom.valuator.math.FinancialMath;

import java.time.Year;

/**
 * Company Valuation
 */
public class ValuatorService {

    public static final int DEFAULT_GROWTH_MULTIPLE = 4;
    public static final int FAST_GROWTH_MULTIPLE = 6;
    public static final int LEADER_GROWTH_MULTIPLE = 8;

    private final CountryData countryData;
    private final CompanyData company;
    private final int exitYear;

    public ValuatorService(CompanyData companyData) {
        this(companyData, Year.now().getValue());

    }

    public ValuatorService(CompanyData companyData, int exitYear) {
        this.company = companyData;
        this.countryData = new CountryData(company.getCountry());
        this.exitYear = exitYear;
    }


    public CountryData getCountryData() {
        return countryData;
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
        if (WACC==0.0) { // if neither external investments nor loans, but only own equity
            // todo calculate beta based on public company data (unlevered beta)
            WACC = FinancialMath.getCAPM(baseRate, 1, marketReturn);
        }
        double DCF = FinancialMath.getDCF(fcf, WACC);
        double TV = FinancialMath.getTerminalValue(fcf[fcf.length-1], WACC, growthRate);
        double NFP = debt - cash;
        double equityValue = DCF + TV - NFP;

        if (logReport) {
            report.append("\n------------------------------------------------------------\n");
            report.append(company.getName());
            report.append(" Discounted Cash Flow (FCF) Valuation\n");
            report.append("------------------------------------------------------------\n");
            report.append("WACC = ").append(FinancialMath.toPercent(WACC)).append("%\n");
            report.append("Economy growth = ").append(FinancialMath.toPercent(growthRate)).append("%\n");
            report.append("DCF = ").append(countryData.formatMoney(DCF)).append("\n");
            report.append("TV = ").append(countryData.formatMoney(TV)).append("\n");
            report.append("NFP = ").append(countryData.formatMoney(NFP)).append("\n");
            report.append("Valuation = ").append(countryData.formatMoney(equityValue)).append("\n");
            //report.append("--------------------------------------------\n");
        }

        return equityValue;
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
        if (company.isLeader()) multiple = LEADER_GROWTH_MULTIPLE;

        double baseEBITDA = beginningValue;

        // find first positive ebitda
        for (int i=0; i<ebitda.length; i++) {
            double value = ebitda[i];
            int year = company.getDataFirstYear() + i;
            if (value > 0 && year >= exitYear) {
                baseEBITDA = value;
                break;
            }
        }

        double enterpriseValue = baseEBITDA * multiple;
        double equityValuation = enterpriseValue - company.getDebt();

        if (logReport) {
            report.append("\n------------------------------------------------------------\n");
            report.append(company.getName());
            report.append(" EBITDA Multiple Valuation\n");
            report.append("------------------------------------------------------------\n");
            report.append("EBITDA: ").append(countryData.formatMoney(baseEBITDA)).append("\n");
            report.append("Growth rate: ").append(Math.round(CAGR*10000.0)/100.0).append("%\n");
            report.append("Multiple: ").append(multiple).append("x\n");
            report.append("Valuation: ").append(countryData.formatMoney(equityValuation)).append("\n");
        }

        return equityValuation;
    }



    public double valuateMultiples(StringBuilder report) {
        try {
            boolean logReport = report != null;
            String listedCompanyTicker = company.getComparableStock();

            StockData sds = new StockData(listedCompanyTicker);
            double EVtoRevenue = sds.getEVToRevenue();
            double EVtoEBITDA = sds.getEVToEBITDA();
            double[] revenue = company.getRevenue();
            double[] ebitda = company.getEBITDA();
            int yearIndex = exitYear - company.getDataFirstYear();
            if (yearIndex < 0) return 0;

            // calculate EV/Revenue and EV/EBITDA valuations if data available
            boolean revenueAvailable = (revenue != null);
            boolean ebitdaAvailable = (ebitda != null && ebitda[yearIndex] > 0 && EVtoEBITDA > 0);
            double EVRevenueValuation = revenueAvailable ? revenue[yearIndex] * EVtoRevenue : 0;
            double EVEBITDAValuation = ebitdaAvailable ? ebitda[yearIndex] * EVtoEBITDA : 0;
            int count = (revenueAvailable ? 1 : 0) + (ebitdaAvailable ? 1 : 0);
            double enterpriseValue = (EVRevenueValuation + EVEBITDAValuation) / count;
            double valuation = enterpriseValue - company.getDebt();

            if (logReport) {
                report.append("\n------------------------------------------------------------\n");
                report.append(company.getName());
                report.append(" Multiples Valuation\n");
                report.append("------------------------------------------------------------\n");
                report.append("Comparable: ").append(sds.getName()).append("\n");
                report.append("EV/Revenue (").append(sds.getEVToRevenue()).append("x): ")
                    .append(countryData.formatMoney(EVRevenueValuation)).append("\n");
                report.append("EV/EBITDA (").append(sds.getEVToEBITDA()).append("x): ")
                    .append(countryData.formatMoney(EVEBITDAValuation)).append("\n");
                report.append("EV average: ").append(countryData.formatMoney(enterpriseValue)).append("\n");
                report.append("Valuation: ").append(countryData.formatMoney(valuation)).append("\n");
            }
            return valuation;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


}
