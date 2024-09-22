package com.axiom.valuator.services;

import com.axiom.valuator.math.FinancialMath;
import com.axiom.valuator.model.CompanyData;
import com.axiom.valuator.model.CountryData;
import com.axiom.valuator.model.ValuatorEngine;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.time.Year;

public class ValuationService implements Route {

    // todo make POST end point to work with JS frontend

    @Override
    public Object handle(Request request, Response response) throws Exception {

        String err = validateRequest(request);
        if (err != null) {
            response.status(400);
            response.body(err);
            return err;
        }

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



    private String validateRequest(Request request) {
       String contentType = request.contentType();
       if (!contentType.equals("application/json")) {
           return "Content-type application/json expected";
       }

       try {
           if (request.contentLength() > 16 * 1024) return "Content exceeds reasonable size";
           String body = request.body();
           JSONObject obj = new JSONObject(body);
           if (!obj.has("name")) return "name field missing";
           if (!obj.has("country")) return "country field missing";
           if (!obj.has("dataFirstYear")) return "dataFirstYear field missing";
           if (!obj.has("revenue")) return "revenue field missing";
           if (!obj.has("ebitda")) return "ebitda field missing";
           if (!obj.has("freeCashFlow")) return "freeCashFlow field missing";
           if (!obj.has("cash")) return "cash field missing";
           if (!obj.has("equity")) return "equity field missing";
           if (!obj.has("equityRate")) return "equityRate field missing";
           if (!obj.has("debt")) return "debt field missing";
           if (!obj.has("debtRate")) return "debtRate field missing";
           if (!obj.has("isLeader")) return "isLeader field missing";
           if (!obj.has("comparableStock")) return "comparableStock field missing";
       } catch (JSONException e) {
            return e.toString();
       }
       return null;
    }


}
