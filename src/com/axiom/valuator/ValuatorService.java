package com.axiom.valuator;

import com.axiom.valuator.data.CompanyData;
import com.axiom.valuator.methods.DCFValuator;

public class ValuatorService {


    public static void main(String[] args) {

        CompanyData arta = new CompanyData("ARTA");
        arta.setFCF(new double[]{120000000.0, 180000000.0, 240000000.0});
        arta.setCash(30000000);
        arta.setDebt(125000000, 0.35);
        arta.setEquity(50000000, 0.24);

        DCFValuator dcf = new DCFValuator("KZ");
        System.out.println(dcf.valuate(arta) / 1000000 + " mln");
    }

}
