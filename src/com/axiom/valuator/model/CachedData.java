//=======================================================================================
//
//  PRIVATE COMPANY VALUATOR
//  Cache for country and stock data
//
// (C) 2024 Axiom Capital, Bolat Basheyev
//=======================================================================================
package com.axiom.valuator.model;

import org.json.JSONObject;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import java.time.LocalDate;
import java.time.Period;


public class CachedData {

    public static final int COUNTRY_DATA_EXPIRATION_PERIOD = 12;  // 12 month (1 year)
    public static final int COMPANY_DATA_EXPIRATION_PERIOD = 3;   // 3 month (1 quarter)

    private static final String DB_PATH = "cache/cached_data.db";
    private static final String DB_COUNTRIES = "countries";
    private static final String DB_COMPANIES = "companies";
    public static final String COMPANY_DATE_FIELD = "LatestQuarter";
    public static final String COUNTRY_DATE_FIELD = "lastYear";

    private static boolean initialized = false;
    private static DB cacheDB;
    private static HTreeMap<String, String> companiesCache;
    private static HTreeMap<String, String> countriesCache;


    private static void initialize() {
        cacheDB = DBMaker.fileDB(DB_PATH).transactionEnable().make();
        companiesCache = (HTreeMap<String, String>) cacheDB.hashMap(DB_COMPANIES).createOrOpen();
        countriesCache = (HTreeMap<String, String>) cacheDB.hashMap(DB_COUNTRIES).createOrOpen();
        initialized = true;
    }


    public static boolean containsCompany(String key) {
        if (!initialized) initialize();
        return companiesCache.containsKey(key);
    }


    public static String getCompany(String key) {
        if (!initialized) initialize();
        if (!companiesCache.containsKey(key)) return null;

        String jsonString = companiesCache.get(key);
        if (jsonString==null) return null;
        JSONObject json = new JSONObject(jsonString);
        if (!json.has(COMPANY_DATE_FIELD)) return null;
        String latestQuarter = json.getString(COMPANY_DATE_FIELD);
        LocalDate reportQuarter = LocalDate.parse(latestQuarter);
        LocalDate today = LocalDate.now();
        Period period = Period.between(reportQuarter, today);
        int monthsDifference = period.getYears() * 12 + period.getMonths();
        if (monthsDifference >= COMPANY_DATA_EXPIRATION_PERIOD) return null;

        return jsonString;
    }


    public static void putCompany(String key, String value) {
        if (!initialized) initialize();
        companiesCache.put(key, value);
        cacheDB.commit();
    }


    public static boolean containsCountry(String key) {
        if (!initialized) initialize();
        return countriesCache.containsKey(key);
    }


    public static String getCountry(String key) {
        if (!initialized) initialize();
        if (!countriesCache.containsKey(key)) return null;

        String jsonString = countriesCache.get(key);
        if (jsonString==null) return null;
        JSONObject json = new JSONObject(jsonString);
        int latestYear = json.getInt(COUNTRY_DATE_FIELD);
        LocalDate reportQuarter = LocalDate.of(latestYear, 12,31);
        LocalDate today = LocalDate.now();
        Period period = Period.between(reportQuarter, today);
        int monthsDifference = period.getYears() * 12 + period.getMonths();
        if (monthsDifference >= COUNTRY_DATA_EXPIRATION_PERIOD) return null;

        return countriesCache.get(key);
    }


    public static void putCountry(String key, String value) {
        if (!initialized) initialize();
        countriesCache.put(key, value);
        cacheDB.commit();
    }

}
