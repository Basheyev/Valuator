package com.axiom.valuator;


import com.axiom.valuator.services.ValuationService;
import spark.Spark;

public class ServerApplication {


    public static void main(String[] args) {

        Spark.port(80);
        Spark.init();
        Spark.get("/hello", new ValuationService());
        Spark.get("/shutdown", (request, response) -> { Spark.stop(); return "Server stopped"; } );

    }


}
