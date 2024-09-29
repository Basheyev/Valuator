package com.axiom.valuator.services;

import com.axiom.valuator.math.FinancialMath;
import com.axiom.valuator.model.CompanyData;
import com.axiom.valuator.model.ValuatorEngine;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.math.BigDecimal;
import java.time.Year;

public class ValuationService implements Route {

    public static final long MAX_CONTENT_SIZE_BYTES = 4096;


    @Override
    public Object handle(Request request, Response response) throws Exception {

        String err = validateRequest(request);
        if (err != null) {
            response.status(400);
            response.body(err);
            return err;
        }

        JSONObject companyJSON = new JSONObject(request.body());

        System.out.println(companyJSON.toString());

        CompanyData company = new CompanyData(companyJSON);
        StringBuilder report = new StringBuilder();

        generateReport(company, report);

        response.status(200);
        response.body(report.toString());
        return report.toString();
    }


    /**
     * Check size and consistency of incoming request
     * @param request request data: header and body
     * @return null if request is validated or error message string
     */
    private String validateRequest(Request request) {
       String contentType = request.contentType();
       if (!contentType.equals("application/json")) {
           return "Content-type application/json expected";
       }

       try {
           if (request.contentLength() > MAX_CONTENT_SIZE_BYTES) {
               return "Content exceeds reasonable size";
           }
           String body = request.body();
           JSONObject obj = new JSONObject(body);
           if (obj.isEmpty()) return "Empty object {}";
           String FIELD_WRONG = "field missing or has wrong type in JSON:\n " + obj.toString(4);
           if (isInvalid(obj, "name", "String")) return "name " + FIELD_WRONG;
           if (isInvalid(obj, "country", "String")) return "country " + FIELD_WRONG;
           if (isInvalid(obj, "dataFirstYear", "Number")) return "dataFirstYear " + FIELD_WRONG;
           if (isInvalid(obj, "revenue", "Number[]")) return "revenue " + FIELD_WRONG;
           if (isInvalid(obj, "ebitda", "Number[]")) return "ebitda " + FIELD_WRONG;
           if (isInvalid(obj, "freeCashFlow", "Number[]")) return "freeCashFlow " + FIELD_WRONG;
           if (isInvalid(obj, "cash", "Number")) return "cash " + FIELD_WRONG;
           if (isInvalid(obj, "equity", "Number")) return "equity " + FIELD_WRONG;
           if (isInvalid(obj, "equityRate", "Number")) return "equityRate " + FIELD_WRONG;
           if (isInvalid(obj, "debt", "Number")) return "debt " + FIELD_WRONG;
           if (isInvalid(obj, "debtRate", "Number")) return "debtRate " + FIELD_WRONG;
           if (isInvalid(obj, "marketShare", "Number")) return "marketShare " + FIELD_WRONG;
           if (isInvalid(obj, "comparableStock", "String")) return "comparableStock " + FIELD_WRONG;
        } catch (JSONException e) {
            return "Failed to parse JSON:\n" + request.body() + "\n" + e.toString();
       }
       return null;
    }


    private boolean isInvalid(JSONObject obj, String fieldName, String fieldType) {
        if (!obj.has(fieldName) || obj.isNull(fieldName)) return true;
        Object field = obj.get(fieldName);
        if ((field instanceof Boolean) && fieldType.equals("Boolean")) return false;
        if ((field instanceof String) && fieldType.equals("String")) return ((String) field).isEmpty();
        if ( (field instanceof BigDecimal) || (field instanceof Integer) || (field instanceof Double)
            && fieldType.equals("Number")) return false;
        if ((field instanceof JSONObject) && fieldType.equals("Object")) return false;
        if ((field instanceof JSONArray) && fieldType.equals("Number[]")) {
            return !isArrayOfNumbers((JSONArray) field);
        }
        return true;
    }


    private boolean isArrayOfNumbers(JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.length(); i++) {
            if (!(jsonArray.get(i) instanceof Number)) {
                return false;
            }
        }
        return true;
    }


    private void generateReport(CompanyData company, StringBuilder report) {

        int exitYear = company.getVentureExitYear();
        // todo validate input data

        report.append(company.toHTML());
        report.append("<hr class=\"my-3\">");

        ValuatorEngine valuatorEngine = new ValuatorEngine(company, exitYear);

        double ebitda = valuatorEngine.valuateEBITDA(report, false);
        report.append("<hr class=\"my-3\">");
        double multiples = valuatorEngine.valuateMultiples(report, false);
        report.append("<hr class=\"my-3\">");
        double dcf = valuatorEngine.valuateDCF(report, false);

        double average = (ebitda * 0.4) + (multiples * 0.3) + (dcf * 0.3); // todo explain

        report.append("<hr class=\"my-3\">");
        report.append("<h5> Exit Value (").append(exitYear).append("): ")
            .append(valuatorEngine.getCountryData().formatMoney(average)).append("</h5>");

        int currentYear = Year.now().getValue();
        int yearsToExit = exitYear - currentYear;

        if (yearsToExit >= 1) {
            double presentValue = FinancialMath.getPresentValue(average, company.getVentureRate(), yearsToExit);
            report.append("<hr class=\"my-3\">");
            report.append("<h5>Present Value (").append(currentYear).append("): ")
                .append(valuatorEngine.getCountryData().formatMoney(presentValue)).append("</h5>");
        }
    }


}
