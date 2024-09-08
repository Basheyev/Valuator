package com.axiom.valuator.math;

import com.axiom.valuator.services.CountryDataService;

import java.text.NumberFormat;
import java.util.Locale;

public class FinancialMath {


    private final double CORPORATE_TAX;
    private final Locale country;
    private final NumberFormat currencyFormatter;
    private final CountryDataService wbs;

    /**
     *
     * @param countryCode country name by Alpha-2 code (ISO3166)
     */
    public FinancialMath(String countryCode) {
        country = CountryDataService.getCountryByCode(countryCode);
        currencyFormatter = NumberFormat.getCurrencyInstance(country);
        wbs = new CountryDataService(countryCode);
        CORPORATE_TAX = wbs.getCorporateTax();

        System.out.println(wbs);
        int begin = wbs.getFirstYear();
        int end = wbs.getLastYear();
        int periods = (end - begin) + 1;
        double cagr = getCAGR(wbs.getGDPValue(begin), wbs.getGDPValue(end), periods) * 100;
        System.out.println("CAGR (%) " + cagr);
    }


    public Locale getCountry() {
        return country;
    }

    /**
     * Calculates compound average growth rate - CAGR (%)
     * @param beginningValue beginning value (non-zero)
     * @param endingValue ending value
     * @param periods number of periods (non-zero)
     * @return CAGR (%) or NaN if ending value or number of periods is zero.
     */
    public double getCAGR(double beginningValue, double endingValue, double periods) {
        if (beginningValue==0 || periods==0) return Double.NaN;
        return Math.pow(endingValue / beginningValue, 1 / periods) - 1.0d;
    }


    /**
     * Calculates Weighted Average Cost of Capital (WACC)
     * @param D debt size
     * @param Dc debt cost (rate)
     * @param E equity size
     * @param Ec equity cost (rate)
     * @return WACC or zero if equity and debt are zero
     */
    public double getWACC(double D, double Dc, double E, double Ec) {
        double V = D + E;
        if (V==0) return 0.0;
        if (D==0 & E!=0) return Ec;
        if (D!=0 & E==0) return Dc;
        return (E/V * Ec) + (D/V * Dc * (1.0 - CORPORATE_TAX));
    }


    /**
     * Calculates discounted cash flow DCF
     * @param fcf array of free cash flow values by periods
     * @param WACC weighted average cost of capital
     * @return discounted cash flow
     */
    public double getDCF(double[] fcf, double WACC) {
        if (fcf.length==0) return 0;
        double sum = 0;
        for (int t=0; t<fcf.length; t++) {
            sum += fcf[t] / Math.pow(1.0 + WACC, t);
        }
        return sum;
    }


    public String formatMoney(double moneyValue) {
        return currencyFormatter.format(moneyValue);
    }

}
