package com.example.healthybuddy.DTO;

import com.google.gson.annotations.SerializedName;

public class RegisterDTO {
    String mId;
    String mPwd;
    String mEmail;

    public RegisterDTO(){}

    public RegisterDTO(String USER_ID, String USER_PWD, String USER_EMAIL){
        this.mId = USER_ID;
        this.mPwd = USER_PWD;
        this.mEmail = USER_EMAIL;
    }

    public String getUSER_ID() {
        return mId;
    }

    public void setUSER_ID(String USER_ID) {
        this.mId=mId;
    }

    public String getUSER_PWD() {
        return mPwd;
    }

    public void setUSER_PWD(String USER_PWD) {
        this.mPwd = mPwd;
    }

    public String getUSER_EMAIL() {
        return mEmail;
    }

    public void setUSER_EMAIL(String USER_EMAIL) {
        this.mEmail = mEmail;
    }

    @Override
    public String toString(){
        return "RegisterDTO{" +
                "mId='" + mId +  '\'' +
                ", mPwd='" + mPwd + '\'' +
                ", mEmail='" + mEmail + '}';
    }
}
