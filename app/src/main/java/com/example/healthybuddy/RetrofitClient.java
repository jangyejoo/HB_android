package com.example.healthybuddy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class RetrofitClient {
    private static final String BASE_URL = "http://Hb-env.eba-4ndxmyvz.ap-northeast-2.elasticbeanstalk.com/";

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    Register register = retrofit.create(Register.class);
    Login login = retrofit.create(Login.class);
    Profile profile = retrofit.create(Profile.class);
}
