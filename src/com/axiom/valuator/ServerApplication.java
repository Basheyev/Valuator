package com.axiom.valuator;


import com.axiom.valuator.services.ValuationService;
import spark.Spark;

public class ServerApplication {


    public static void main(String[] args) {

        Spark.port(80);
        Spark.staticFileLocation("/main/resources/public");
        Spark.init();
        Spark.post("/valuate", new ValuationService());
        Spark.get("/shutdown", (request, response) -> { Spark.stop(); return "Server stopped"; } );

    }


}
