package com.axiom.valuator;

import com.axiom.valuator.math.FinancialMath;


public class Valuator {


    public static void main(String[] args) {
        FinancialMath fm = new FinancialMath("CN");
        double[] fcf = {200000000,450000000,830000000};
        double cagr = fm.getCAGR(fcf[0], fcf[2], 2);
        double wacc = fm.getWACC(125000000, 0.35, 80000000, 0.58);
        System.out.println("CAGR (%): " + cagr * 100.0);
        System.out.println("WACC (%): " + wacc * 100.0);
        System.out.println("DCF: " + fm.formatMoney(fm.getDCF(fcf, wacc)));

    }

}
