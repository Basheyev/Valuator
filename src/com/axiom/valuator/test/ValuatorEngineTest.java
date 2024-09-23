package com.axiom.valuator.test;

import com.axiom.valuator.model.CompanyData;
import com.axiom.valuator.math.FinancialMath;
import com.axiom.valuator.model.ValuatorEngine;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Year;

public class ValuatorEngineTest {

    public static void testValuation(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        JSONObject jsonObject = new JSONObject(content);
        CompanyData company = new CompanyData(jsonObject);

        int exitYear = 2024;
        StringBuilder sb = new StringBuilder();
        sb.append(company);
        ValuatorEngine valuatorEngine = new ValuatorEngine(company, exitYear);
        double dcf = valuatorEngine.valuateDCF(sb);
        double ebitda = valuatorEngine.valuateEBITDA(sb);
        double multiples = valuatorEngine.valuateMultiples(sb);
        double factors = (ebitda > 0 ? 1 : 0) + (multiples > 0 ? 1 : 0) + (dcf > 0 ? 1 : 0);
        double average = (dcf + ebitda + multiples) / factors;

        sb.append("\n------------------------------------------------------------\n");
        sb.append("VALUATION AVERAGE (").append(exitYear).append("): ")
            .append(valuatorEngine.getCountryData().formatMoney(average)).append("\n");
        int currentYear = Year.now().getValue();
        int yearsToExit = exitYear - currentYear;
        if (yearsToExit >= 1) {
            double presentValue = FinancialMath.getPresentValue(average, 0.58, yearsToExit);
            sb.append("Present Value (").append(currentYear).append("): ")
                .append(valuatorEngine.getCountryData().formatMoney(presentValue)).append("\n");
        }
        System.out.println(sb);
    }


    public static void main(String[] args) throws IOException {
        testValuation("data/arta.json");
    }

}
