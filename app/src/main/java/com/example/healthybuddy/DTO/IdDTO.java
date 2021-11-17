package com.example.healthybuddy.DTO;

public class IdDTO {
    private String mId;

    public IdDTO(){}

    public IdDTO(String USER_ID){
        this.mId = USER_ID;
    }

    public String getUSER_ID() {
        return mId;
    }

    public void setUSER_ID(String USER_ID) {
        this.mId=mId;
    }

}
