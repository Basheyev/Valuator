package com.axiom.valuator;

import com.axiom.valuator.data.CompanyData;
import com.axiom.valuator.data.CountryData;
import com.axiom.valuator.math.FinancialMath;
import com.axiom.valuator.services.ValuatorService;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Valuator {

    public static void testValuation(String filePath) throws IOException {

        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        JSONObject jsonObject = new JSONObject(content);
        CompanyData company = new CompanyData(jsonObject);

        StringBuilder sb = new StringBuilder();
        sb.append(company);
        ValuatorService valuator = new ValuatorService(company);
        double dcf = valuator.valuateDCF(sb);
        double ebitda = valuator.valuateEBITDA(sb);
        double multiples = valuator.valuateMultiples(sb);
        double average = (dcf + ebitda + multiples) / 3;
        sb.append("\n------------------------------------------------------------\n");
        sb.append("VALUATION AVERAGE: ").append(valuator.getCountryData().formatMoney(average)).append("\n");
        System.out.println(sb);
    }

    public static void main(String[] args) throws IOException {
        testValuation("data/innoforce.json");

    }

}
