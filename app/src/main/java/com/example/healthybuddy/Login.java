package com.example.healthybuddy;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface Login {
    @POST("login")
    Call<LoginResponse> goPost(@Body LoginRequest objJson);

    @GET("members")
   Call<List<RegisterDTO>> members(@Header("Authorization") String authorization);
}

