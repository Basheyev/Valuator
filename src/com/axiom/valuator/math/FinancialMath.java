package com.axiom.valuator.math;


public class FinancialMath {

    /**
     * Calculates compound average growth rate - CAGR (%)
     * @param beginningValue beginning value (non-zero)
     * @param endingValue ending value
     * @param periods number of periods (non-zero)
     * @return CAGR (%) or NaN if ending value or number of periods is zero.
     */
    public static double getCAGR(double beginningValue, double endingValue, double periods) {
        if (beginningValue==0 || periods==0) return Double.NaN;
        return Math.pow(endingValue / beginningValue, 1 / periods) - 1.0d;
    }


    /**
     * Calculates Weighted Average Cost of Capital (WACC)
     * @param D debt size
     * @param Dc debt cost (rate)
     * @param E equity size
     * @param Ec equity cost (rate)
     * @param corporateTax corporate income tax rate
     * @return WACC or zero if equity and debt are zero
     */
    public static double getWACC(double D, double Dc, double E, double Ec, double corporateTax) {
        double V = D + E;
        if (V==0) return 0.0;
        if (D==0 & E!=0) return Ec;
        if (D!=0 & E==0) return Dc;
        return (E/V * Ec) + (D/V * Dc * (1.0 - corporateTax));
    }


    /**
     * Calculates Capital Asset Pricing Model (CAPM)
     * @param riskFreeRate risk-free rate (base rate)
     * @param beta company sensitivity to market volatility
     * @param marketReturn expected market return
     * @return cost of capital using Capital Asset Pricing Model
     */
    public static double getCAPM(double riskFreeRate, double beta, double marketReturn) {
        return riskFreeRate + beta * (marketReturn - riskFreeRate);
    }



    /**
     * Calculates discounted cash flow (DCF)
     * @param fcf array of free cash flow values by periods
     * @param WACC weighted average cost of capital
     * @return discounted cash flow
     */
    public static double getDCF(double[] fcf, double WACC) {
        if (fcf.length==0) return 0;
        double sum = 0;
        for (int t=0; t<fcf.length; t++) {
            sum += getPresentValue(fcf[t], WACC, 1 + t);
        }
        return sum;
    }


    /**
     * Calculates terminal value
     * @param lastFCF last year free cash flow
     * @param WACC weighted average cost of capital
     * @param growthRate average GDP growth rate
     * @return terminal value or NaN if growth rate higher than WACC
     */
    public static double getTerminalValue(double lastFCF, double WACC, double growthRate) {
        if (growthRate > WACC) return Double.NaN;
        return (lastFCF * (1 + growthRate)) / (WACC - growthRate);
    }


    /**
     * Calculates present value by discounting exit value
     * @param exitValue projected exit value
     * @param WACC weighted average cost of capital
     * @param periods number of periods till exit
     * @return present value
     */
    public static double getPresentValue(double exitValue, double WACC, int periods) {
        return exitValue / Math.pow(1.0 + WACC, periods);
    }

}
