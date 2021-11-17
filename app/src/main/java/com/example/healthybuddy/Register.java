package com.example.healthybuddy;

import com.example.healthybuddy.DTO.EmailDTO;
import com.example.healthybuddy.DTO.IdDTO;
import com.example.healthybuddy.DTO.RegisterDTO;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface Register {
    @POST("signup")
    Call<ResponseBody> goPost(@Body RegisterDTO objJson);

    @POST("idcheck")
    Call<ResponseBody> idCheck(@Body IdDTO objJon);

    @POST("emailcheck")
    Call<ResponseBody> emailCheck(@Body EmailDTO objJon);
}
