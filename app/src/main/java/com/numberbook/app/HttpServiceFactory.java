package com.numberbook.app;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpServiceFactory {

    private static final String API_ROOT = "http://10.0.2.2/numberbook-api/api/";
    private static Retrofit sharedInstance;

    private HttpServiceFactory() {
    }

    public static Retrofit obtainInstance() {
        if (sharedInstance == null) {
            sharedInstance = new Retrofit.Builder()
                    .baseUrl(API_ROOT)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return sharedInstance;
    }
}
