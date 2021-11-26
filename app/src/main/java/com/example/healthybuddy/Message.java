package com.example.healthybuddy;

import com.example.healthybuddy.DTO.ChatDTO;
import com.example.healthybuddy.DTO.MessageDTO;

import java.util.HashMap;
import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;

public interface Message {
    @Multipart
    @POST("msg")
    Call<List<MessageDTO>> list (@Header("Authorization") String authorization, @PartMap HashMap<String, RequestBody> data);

    @Multipart
    @POST("create_msg")
    Call<ResponseBody> create (@Header("Authorization") String authorization, @PartMap HashMap<String, RequestBody> data);
}
