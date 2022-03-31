package com.example.healthybuddy.DTO;

public class PwdDTO {
    String mId;
    String mPwd;
    String newPwd;

    public PwdDTO(){}

    public PwdDTO(String id, String pwd, String newPwd){
        this.mId = id;
        this.mPwd = pwd;
        this.newPwd = newPwd;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getmPwd() {
        return mPwd;
    }

    public void setmPwd(String mPwd) {
        this.mPwd = mPwd;
    }

    public String getNewPwd() {
        return newPwd;
    }

    public void setNewPwd(String newPwd) {
        this.newPwd = newPwd;
    }
}
