package com.axiom.valuator;

import com.axiom.valuator.math.FinancialMath;
import com.axiom.valuator.services.CountryDataService;
import com.axiom.valuator.services.StockDataService;


public class Valuator {


    public static void main(String[] args) {

        CountryDataService cds = new CountryDataService("KZ", 5);
        System.out.println(cds);

       // StockDataService stock = new StockDataService("AAPL");
      //  System.out.println(stock);
    }

}
