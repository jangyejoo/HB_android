package com.example.healthybuddy;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public interface Profile {
    /*
    @POST("create_profile")
    Call<ResponseBody> create(@Header("Authorization") String authorization, @Body ProfileDTO objJson);
     */
    @Multipart
    @POST("create_profile")
    Call<ResponseBody> create(@Header("Authorization") String authorization, @Part MultipartBody.Part pImg, @PartMap HashMap<String, RequestBody> data);

    @Multipart
    @POST("profile_image")
    Call<ResponseBody> image (@Header("Authorization") String authorization, @Part MultipartBody.Part pImg, @PartMap HashMap<String, RequestBody> data);

}
