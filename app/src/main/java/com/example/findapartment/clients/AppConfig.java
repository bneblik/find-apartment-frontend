package com.example.findapartment.clients;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AppConfig {
    private static String BASE_URL_LOCAL = "http://10.0.2.2:3000/api/";
    private static String BASE_URL = "https://findapartmentserver.herokuapp.com/api/";

    public static String getBaseUrl() {
        return BASE_URL_LOCAL;
    }

    public static Retrofit getRetrofit() {
        return new Retrofit.Builder().baseUrl(AppConfig.getBaseUrl()).addConverterFactory(GsonConverterFactory.create()).build();
    }
}
