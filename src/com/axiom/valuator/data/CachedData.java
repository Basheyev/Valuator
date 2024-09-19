package com.axiom.valuator.data;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

public class CachedData {

    private static final String DB_PATH = "cache/cached_data.db";
    private static final String DB_COUNTRIES = "countries";
    private static final String DB_COMPANIES = "companies";

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
        return companiesCache.get(key);
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
        return countriesCache.get(key);
    }


    public static void putCountry(String key, String value) {
        if (!initialized) initialize();
        countriesCache.put(key, value);
        cacheDB.commit();
    }

}
