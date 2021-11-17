package com.example.healthybuddy.DTO;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("result")
    public String resultCode;

    @SerializedName("token")
    public String token;

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
