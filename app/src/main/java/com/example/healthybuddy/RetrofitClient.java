package com.example.healthybuddy;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://Hb-env.eba-4ndxmyvz.ap-northeast-2.elasticbeanstalk.com/";

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    Register register = retrofit.create(Register.class);
    Login login = retrofit.create(Login.class);
    Profile profile = retrofit.create(Profile.class);
    Chat chat = retrofit.create(Chat.class);
    Message message = retrofit.create(Message.class);
    Mate mate = retrofit.create(Mate.class);
}
