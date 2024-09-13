package com.axiom.valuator;

import com.axiom.valuator.data.CompanyData;
import com.axiom.valuator.services.CountryDataService;
import com.axiom.valuator.services.ValuatorService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Locale;

public class Valuator {

    public static void testARTA() throws IOException {

        Locale region = CountryDataService.getCountryByCode("KZ");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(region);

        String filePath = "data/cargon.json";
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        JSONObject jsonObject = new JSONObject(content);
        CompanyData cargon = new CompanyData(jsonObject);

        StringBuilder sb = new StringBuilder();
        sb.append(cargon);
        ValuatorService valuator = new ValuatorService(cargon);
        double dcf = valuator.valuateDCF(sb);
        double ebitda = valuator.valuateEBITDA(sb);
        double multiples = valuator.valuateMultiples("MSFT", sb);
        System.out.println(sb);
    }

    public static void main(String[] args) throws IOException {
        testARTA();
    }

}
