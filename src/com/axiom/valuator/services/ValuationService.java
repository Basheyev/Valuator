package com.axiom.valuator.services;

import spark.Request;
import spark.Response;
import spark.Route;

public class ValuationService implements Route {

    // todo make POST end point to work with JS frontend

    @Override
    public Object handle(Request request, Response response) throws Exception {
        return "Hello, world!";
    }

}
