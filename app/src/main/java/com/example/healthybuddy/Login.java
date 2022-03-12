package com.example.healthybuddy;

import com.example.healthybuddy.DTO.AutoLoginResponse;
import com.example.healthybuddy.DTO.LoginRequest;
import com.example.healthybuddy.DTO.LoginResponse;
import com.example.healthybuddy.DTO.RegisterDTO;

import java.util.HashMap;
import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;

public interface Login {
    @POST("login")
    Call<LoginResponse> goPost(@Body LoginRequest objJson);

    @GET("member")
    Call<List<RegisterDTO>> members(@Header("Authorization") String authorization);

    @Multipart
    @POST("find_id")
    Call<ResponseBody> findId (@PartMap HashMap<String, RequestBody> data);

    @Multipart
    @POST("find_pwd")
    Call<ResponseBody> findPwd (@PartMap HashMap<String, RequestBody> data);

    @POST("issue")
    Call<AutoLoginResponse> autoLogin (@Header("jwt")String token, @Header("mId")String mId);

    @DELETE("logout")
    Call<Void> logout (@Header("jwt")String token, @Header("mId")String mId);

}

