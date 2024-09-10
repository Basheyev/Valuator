package com.axiom.valuator;

import com.axiom.valuator.data.CompanyData;
import com.axiom.valuator.methods.Valuator;
import com.axiom.valuator.services.CountryDataService;

import java.text.NumberFormat;
import java.util.Locale;

public class ValuatorService {


    public static void main(String[] args) {

        Locale region = CountryDataService.getCountryByCode("KZ");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(region);

        CompanyData arta = new CompanyData("ARTA");
        arta.setRevenue(new double[]{830_000_000.0, 1_200_000_000.0, 1_500_000_000.0});
        arta.setFCF(new double[]{120_000_000.0, 180_000_000.0, 240_000_000.0});
        arta.setEBITDA(new double[]{200_000_000.0, 260_000_000.0, 300_000_000.0});
        arta.setCash(30_000_000);
        arta.setDebt(125_000_000, 0.35);
        arta.setEquity(50_000_000, 0.24);

        double dcf = Valuator.valuateDCF(arta, "KZ");
        double multiples = Valuator.valuateMultiples(arta, "APPN");
        double ebitda = Valuator.valuateEBITDA(arta);
        double average = (dcf + multiples + ebitda) / 3.0;

        System.out.println("------------------------------------------");
        System.out.println(arta.getName());
        System.out.println("------------------------------------------");
        System.out.println("DCF: " + currencyFormatter.format(dcf));
        System.out.println("MULTIPLES: " + currencyFormatter.format(multiples));
        System.out.println("EBITDA MULTIPLES: " + currencyFormatter.format(ebitda));
        System.out.println("------------------------------------------");
        System.out.println("Valuation: " + currencyFormatter.format(average));


    }

}
