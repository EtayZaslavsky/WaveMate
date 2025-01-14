package com.etayzas.android.apiresttest.data.remote;

/**
 * Created by Ewise on 05/01/2018.
 */

public class ApiUtils {

    public static final String BASE_URL = "http://goodvibesurfing.me/api/v1/";

    public static SOService getSOService() {
        return RetrofitClient.getClient(BASE_URL).create(SOService.class);
    }
}