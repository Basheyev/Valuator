package com.axiom.valuator;

import com.axiom.valuator.data.CompanyData;
import com.axiom.valuator.services.CountryDataService;
import com.axiom.valuator.services.ValuatorService;

import java.text.NumberFormat;
import java.util.Locale;

public class Valuator {

    public static void testARTA() {

        Locale region = CountryDataService.getCountryByCode("KZ");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(region);

        CompanyData arta = new CompanyData("ARTA", "KZ", 2024);
        arta.setRevenue(new double[]{830_000_000.0, 1_200_000_000.0, 1_500_000_000.0, 1_800_000_000.0})
            .setEBITDA(new double[]{200_000_000.0, 260_000_000.0, 300_000_000.0, 480_000_000.0})
            .setFreeCashFlow(new double[]{120_000_000.0, 180_000_000.0, 240_000_000.0, 300_000_000.0})
            .setCashAndEquivalents(30_000_000)
            .setDebt(125_000_000, 0.35)
            .setEquity(50_000_000, 0.58);

        StringBuilder sb = new StringBuilder();
        sb.append(arta);
        double dcf = ValuatorService.valuateDCF(arta, "KZ", sb);

        System.out.println(sb);
    }

    public static void main(String[] args) {
        testARTA();
    }

}
