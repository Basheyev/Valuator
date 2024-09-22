package com.axiom.valuator.services;

import com.axiom.valuator.math.FinancialMath;
import com.axiom.valuator.model.CompanyData;
import com.axiom.valuator.model.ValuatorEngine;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.time.Year;

public class ValuationService implements Route {

    // todo make POST end point to work with JS frontend

    @Override
    public Object handle(Request request, Response response) throws Exception {

        JSONObject companyJSON = new JSONObject(request.body());
        System.out.println(companyJSON.toString(4));

        CompanyData company = new CompanyData(companyJSON);
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


        response.status(200);
        response.body(sb.toString());

        System.out.println(sb);

        return sb.toString();
    }

}
