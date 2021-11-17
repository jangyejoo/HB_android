package com.example.healthybuddy.DTO;

public class EmailDTO {
    private String mEmail;

    public EmailDTO(){}

    public EmailDTO(String USER_MAIL){
        this.mEmail = USER_MAIL;
    }

    public String getUSER_MAIL() {
        return mEmail;
    }

    public void setUSER_MAIL(String USER_MAIL) {
        this.mEmail=mEmail;
    }
}
