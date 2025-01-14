package com.etayzas.android.apiresttest.data.remoteGeocoding;

import com.etayzas.android.apiresttest.data.model.geocoding.GeocodingResults;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Ewise on 20/02/2018.
 */

public interface GeoCodeService {

    // Get GPS from Address
    @GET("geocode/json?key=AIzaSyAWAnL4eDFoniJjLj-Ddt6J_JRB4q6gI_s")
    Call<GeocodingResults> geocode(@Query("address") String address);
}
