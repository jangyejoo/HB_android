package com.example.healthybuddy.DTO;

import com.google.gson.annotations.SerializedName;

public class AutoLoginResponse {
    @SerializedName("result")
    public String resultCode;

    @SerializedName("jwt")
    public String token;

    @SerializedName("jwt_refresh")
    public String token_refresh;

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

    public String getRefreshToken() {
        return token_refresh;
    }

    public void setRefreshToken(String token_refresh) {
        this.token_refresh = token_refresh;
    }

}
