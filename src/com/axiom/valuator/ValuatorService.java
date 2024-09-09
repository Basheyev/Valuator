package com.axiom.valuator;

import com.axiom.valuator.data.CompanyData;
import com.axiom.valuator.methods.DCFValuator;
import com.axiom.valuator.services.CountryDataService;
import com.axiom.valuator.services.StockDataService;
import java.util.Locale;

public class ValuatorService {


    public static void main(String[] args) {

        CompanyData arta = new CompanyData("ARTA");
        arta.setFreeCashFlow(new double[]{180000000.0, 240000000.0, 360000000.0});
        arta.setDebt(125000000);
        arta.setDebtRate(0.35);
        DCFValuator dcf = new DCFValuator("KZ");
        System.out.println(dcf.valuate(arta) / 1000000 + " mln");
    }

}
