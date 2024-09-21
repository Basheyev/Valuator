package com.axiom.valuator;


import com.axiom.valuator.services.ValuatorService;
import spark.Spark;

public class Valuator {


    public static void main(String[] args) {

        Spark.port(80);
        Spark.init();
        Spark.get("/hello", new ValuatorService());
        Spark.get("/shutdown", (request, response) -> { Spark.stop(); return "Server stopped"; } );

    }


}
