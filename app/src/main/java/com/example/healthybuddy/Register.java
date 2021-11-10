package com.example.healthybuddy;

import java.util.HashMap;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;
import retrofit2.http.Query;

public interface Register {
    @POST("signup")
    Call<ResponseBody> goPost(@Body RegisterDTO objJson);

    @POST("idcheck")
    Call<ResponseBody> idCheck(@Body IdDTO objJon);

    @POST("emailcheck")
    Call<ResponseBody> emailCheck(@Body EmailDTO objJon);
}
