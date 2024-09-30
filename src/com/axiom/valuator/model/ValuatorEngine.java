package com.axiom.valuator.model;

import com.axiom.valuator.math.FinancialMath;

import java.time.Year;

/**
 * Company Valuation
 */
public class ValuatorEngine {

    public static final double MIN_EBITDA_MULTIPLE = 1.5;
    public static final double MAX_EBITDA_MULTIPLE = 15.0;

    private final CountryData countryData;
    private final CompanyData company;
    private final int exitYear;

    public ValuatorEngine(CompanyData companyData) {
        this(companyData, Year.now().getValue());

    }

    public ValuatorEngine(CompanyData companyData, int exitYear) {
        this.company = companyData;
        this.countryData = new CountryData(company.getCountry());
        this.exitYear = exitYear;
    }


    public CountryData getCountryData() {
        return countryData;
    }

    /**
     * Discounted Cash Flow method valuator
     * @param report string builder to write report
     * @param plainText if true writes plaint text report, otherwise HTML
     * @return company valuation
     */
    public double valuateDCF(StringBuilder report, boolean plainText) {
        boolean logReport = report != null;

        double[] fcf = company.getFreeCashFlow();
        double cash = company.getCashAndEquivalents();
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
            if (plainText) {
                report.append("\n------------------------------------------------------------\n");
                report.append(company.getName());
                report.append(" Discounted Cash Flow (FCF) Valuation\n");
                report.append("------------------------------------------------------------\n");
                report.append("Economy growth = ").append(FinancialMath.toPercent(growthRate))
                    .append("%").append(" (").append(countryData.getCountryName()).append(")\n");
                report.append("Corporate Tax = ").append(FinancialMath.toPercent(corporateTax))
                    .append("%").append(" (").append(countryData.getCountryName()).append(")\n");
                report.append("WACC = ").append(FinancialMath.toPercent(WACC)).append("%\n");
                report.append("DCF = ").append(countryData.formatMoney(DCF)).append("\n");
                report.append("TV = ").append(countryData.formatMoney(TV)).append("\n");
                report.append("NFP = ").append(countryData.formatMoney(NFP)).append("\n");
                report.append("Valuation = ").append(countryData.formatMoney(equityValue)).append("\n");
            } else {
                report.append("<p>");
                report.append("<h5>Discounted Cash Flow - ")
                      .append(countryData.formatMoney(equityValue)).append("</h5>");

                report.append("Discounted Cash Flow: <b>")
                    .append(countryData.formatMoney(DCF))
                    .append("</b>")
                    .append(" (WACC: ").append(FinancialMath.toPercent(WACC)).append("%)<br>");

                report.append("Terminal Value: <b>")
                    .append(countryData.formatMoney(TV))
                    .append("</b> (GDP growth: ").append(FinancialMath.toPercent(growthRate)).append("%, ")
                    .append("Tax: ").append(FinancialMath.toPercent(corporateTax)).append("%)")
                    .append("<br>");
                report.append("Net Financial Position: <b>").append(countryData.formatMoney(NFP)).append("</b><br>");
                report.append("</p>");

            }
            //report.append("--------------------------------------------\n");
        }

        return equityValue;
    }


    /**
     * EBITDA multiplier method valuator
     * @param report string builder to write report
     * @param plainText if true writes plaint text report, otherwise HTML
     * @return company valuation
     */
    public double valuateEBITDA(StringBuilder report, boolean plainText) {
        boolean logReport = report != null;

        // Check if EBITDA is available
        double[] ebitda = company.getEBITDA();
        if (ebitda==null || ebitda.length == 0) return 0;

        // Calculate compound average growth rate (CAGR)
        int firstYear = company.getDataFirstYear();
        int lastYear = firstYear + company.getEBITDA().length - 1;

        double beginningValue = ebitda[0];
        double endingValue = ebitda[ebitda.length-1];
        int periods = ebitda.length-1;
        double marketShare = company.getMarketShare();
        double CAGR = FinancialMath.getCAGR(beginningValue, endingValue, periods);

        // Evaluate multiple based on net growth rate and market share
        double inflationRate = countryData.getAverageInflationRate();
        double netGrowthRate = CAGR - inflationRate;
        double growthMultiple = netGrowthRate * 10.0;
        double marketShareMultiple = marketShare * 10.0;
        double multiple = MIN_EBITDA_MULTIPLE + growthMultiple + marketShareMultiple;
        if (multiple > MAX_EBITDA_MULTIPLE) multiple = MAX_EBITDA_MULTIPLE;
        double NFP = company.getDebt() - company.getCashAndEquivalents();
        double baseEBITDA = beginningValue;

        // find first positive ebitda
        int baseEBITDAYear = company.getDataFirstYear();
        for (int i=0; i<ebitda.length; i++) {
            double value = ebitda[i];
            int year = company.getDataFirstYear() + i;
            if (value > 0 && year >= exitYear) {
                baseEBITDA = value;
                baseEBITDAYear = year;
                break;
            }
        }

        double enterpriseValue = baseEBITDA * multiple;
        double equityValue = enterpriseValue - NFP;

        if (logReport) {
            if (plainText) {
                report.append("\n------------------------------------------------------------\n");
                report.append(company.getName());
                report.append(" EBITDA Multiple Valuation\n");
                report.append("------------------------------------------------------------\n");
                report.append("EBITDA: ").append(countryData.formatMoney(baseEBITDA)).append("\n");
                report.append("CAGR (").append(firstYear).append("-").append(lastYear).append("): ")
                    .append(Math.round(CAGR * 10000.0) / 100.0).append("%\n");
                report.append("Inflation: ").append(FinancialMath.toPercent(countryData.getAverageInflationRate())).append("\n");
                report.append("Net Growth Rate: ").append(FinancialMath.toPercent(netGrowthRate)).append("%\n");
                report.append("Market Share: ").append(FinancialMath.toPercent(marketShare)).append("%<\n");
                report.append("Multiple: ").append(Math.round(multiple * 10000.0) / 100.0).append("x\n");
                report.append("Valuation: ").append(countryData.formatMoney(equityValue)).append("\n");
            } else {
                report.append("<p>");
                report.append("<h5>EBITDA Multiple - ").append(countryData.formatMoney(equityValue)).append("</h5>");
                report.append("EBITDA: <b>").append(countryData.formatMoney(baseEBITDA))
                    .append("</b> (").append(baseEBITDAYear).append(")&nbsp<br>");
                report.append("CAGR (").append(firstYear).append("-").append(lastYear).append("): <b>")
                    .append(Math.round(CAGR * 10000.0) / 100.0).append("%</b>&nbsp");
                report.append("Inflation: <b>").append(FinancialMath.toPercent(countryData.getAverageInflationRate())).append("%</b><br>");
                report.append("Net Growth Rate: <b>").append(FinancialMath.toPercent(netGrowthRate)).append("%</b>&nbsp");
                report.append("Market Share: <b>").append(FinancialMath.toPercent(marketShare)).append("%</b><br>");
                report.append("Net Financial Position: <b>").append(countryData.formatMoney(NFP)).append("</b><br>");
                report.append("Multiple: <b>").append(Math.round(multiple * 100.0) / 100.0).append("x</b><br>");
                report.append("</p>");
            }
        }

        return equityValue;
    }


    /**
     * Comparable Multiplier method valuator
     * @param report string builder to write report
     * @param plainText if true writes plaint text report, otherwise HTML
     * @return company valuation
     */
    public double valuateMultiples(StringBuilder report, boolean plainText) {
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
            double enterpriseValue = (EVRevenueValuation + EVEBITDAValuation) / 2.0;
            double NFP = company.getDebt() - company.getCashAndEquivalents();
            double equityValue = enterpriseValue - NFP;

            if (logReport) {
                if (plainText) {
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
                    report.append("Valuation: ").append(countryData.formatMoney(equityValue)).append("\n");
                } else {
                    report.append("<p>");
                    report.append("<h5>Comparable Multiples - ")
                        .append(countryData.formatMoney(equityValue)).append("</h5>");
                    report.append("Comparable: <b>")
                        .append(sds.getName()).append(" (").append(company.getComparableStock()).append(")</b><br>");
                    report.append("EV/Revenue (<b>").append(sds.getEVToRevenue()).append("x</b>): <b>")
                        .append(countryData.formatMoney(EVRevenueValuation)).append("</b><br>");
                    report.append("EV/EBITDA (<b>").append(sds.getEVToEBITDA()).append("x</b>): <b>")
                        .append(countryData.formatMoney(EVEBITDAValuation)).append("</b><br>");
                    report.append("Enterprise Value Average: <b>")
                        .append(countryData.formatMoney(enterpriseValue)).append("</b><br>");
                    report.append("Net Financial Position: <b>").append(countryData.formatMoney(NFP)).append("</b><br>");
                    report.append("</p>");
                }
            }
            return equityValue;
        } catch (Exception e) {
            e.printStackTrace();
            report.append("<p>");
            report.append("<h5>Comparable Multiples - not available</h5>");
            report.append("Data for public company stock '")
                .append(company.getComparableStock())
                .append("' is not available (Alpha Vantage)<br>");
            report.append("</p>");
        }
        return 0;
    }


}
