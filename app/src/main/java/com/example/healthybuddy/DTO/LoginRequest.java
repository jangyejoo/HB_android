package com.example.healthybuddy.DTO;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    @SerializedName("username")
    public String username;

    @SerializedName("password")
    public String password;

    public String getInputId() {
        return username;
    }

    public String getInputPw() {
        return password;
    }

    public void setInputId(String inputId) {
        this.username = inputId;
    }

    public void setInputPw(String inputPwd) {
        this.password = inputPwd;
    }

    public LoginRequest(String inputId, String inputPwd) {
        this.username = inputId;
        this.password = inputPwd;
    }
}
