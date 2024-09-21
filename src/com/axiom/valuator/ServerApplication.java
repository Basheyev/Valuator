package com.axiom.valuator;


import com.axiom.valuator.services.RootService;
import com.axiom.valuator.services.ValuationService;
import org.eclipse.jetty.util.log.Log;
import org.slf4j.simple.SimpleLoggerFactory;
import spark.Spark;
import spark.resource.ClassPathResourceHandler;

public class ServerApplication {


    public static void main(String[] args) {

        Spark.port(80);
        Spark.staticFileLocation("/public");
        Spark.init();
        Spark.get("/root", (req, res) -> new RootService());
        Spark.get("/valuate", new ValuationService());
        Spark.get("/shutdown", (request, response) -> { Spark.stop(); return "Server stopped"; } );

    }


}
