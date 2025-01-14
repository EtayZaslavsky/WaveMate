package com.etayzas.android.apiresttest.data.remote;

/**
 * Created by Ewise on 05/01/2018.
 */

import com.etayzas.android.apiresttest.data.model.LoginRequest;
import com.etayzas.android.apiresttest.data.model.LoginResponse;
import com.etayzas.android.apiresttest.data.model.GooviesResponse;
import com.etayzas.android.apiresttest.data.model.RegisterResponse;
import com.etayzas.android.apiresttest.data.model.RegisterRequest;
import com.etayzas.android.apiresttest.data.model.UploadRequest;
import com.etayzas.android.apiresttest.data.model.UploadResponse;
import com.etayzas.android.apiresttest.data.model.beach.AddBeachRequest;
import com.etayzas.android.apiresttest.data.model.beach.AddBeachResponse;
import com.etayzas.android.apiresttest.data.model.beach.BeachPost;
import com.etayzas.android.apiresttest.data.model.beach.BeachPostResponse;
import com.etayzas.android.apiresttest.data.model.beach.BeachesResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SOService {

    // user
    @POST("register")
    Call<RegisterResponse> register(@Body RegisterRequest registerReq);

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest loginReq);

    @POST("goovy")
    Call<UploadResponse> upload(@Header("authorization") String authorization, @Body UploadRequest uploadReq);

    @GET("goovies/all")
    Call<GooviesResponse> play(@Header("authorization") String authorization);

    /*
    @POST("beaches")
    Call<BeachPostResponse> postBeach(@Header("authorization") String authorization, @Body BeachPost beachPost);
    */

    @GET("goovies/beach/{beach_id}")
    Call<GooviesResponse> playByBeach(
            @Header("authorization") String authorization,
            @Path(value = "beach_id", encoded = true) int beach_id
    );

    @GET("user/beaches")
    Call<BeachesResponse> getUserBeaches(@Header("authorization") String authorization);

    @POST("beach/add")
    Call<AddBeachResponse> addBeach(
            @Header("authorization") String authorization,
            @Body AddBeachRequest beachReq
    );
}