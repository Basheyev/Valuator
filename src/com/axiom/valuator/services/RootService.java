package com.axiom.valuator.services;

import com.axiom.valuator.ServerApplication;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RootService implements Route {

    @Override
    public Object handle(Request request, Response response) throws Exception {

        InputStream inputStream = ServerApplication.class.getResourceAsStream("/public/index.html");

        if (inputStream != null) {
            // Read the resource file (e.g., using BufferedReader or Properties)
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Resource not found!");
        }

        return "static page";
    }
}
