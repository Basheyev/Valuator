package com.axiom.valuator.math;


public class FinancialMath {

    /**
     * Calculates compound average growth rate - CAGR (%)
     * @param beginningValue beginning value greater than zero
     * @param endingValue ending value greater than zero
     * @param periods number of periods greater than zero
     * @return CAGR (%) or NaN if one of argument are zero or negative
     */
    public static double getCAGR(double beginningValue, double endingValue, double periods) {
        if (periods==0) return 0.0;
        return Math.pow(endingValue / beginningValue, 1.0 / periods) - 1.0d;
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
        if (V==0) return 0.0;       // Return zero if there is no debt or equity
        if (D==0 & E!=0) return Ec; // Return cost of equity if there is no debt
        if (D!=0 & E==0) return Dc; // Return cost of debt if there is no eqiuty
        return (E / V * Ec) + (D / V * Dc * (1.0 - corporateTax));
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
        double value;
        for (int t=0; t<fcf.length; t++) {
            value = getPresentValue(fcf[t], WACC, 1 + t);
            sum += value;
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
        if (growthRate >= WACC) return Double.NaN;
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
