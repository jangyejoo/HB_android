package com.example.healthybuddy.DTO;

public class ChatDTO {
    String crId;
    String cId;
    String pNickname;
    String pImg;

    public ChatDTO(){}

    public ChatDTO(String cId, String pNickname, String pImg, String crId){
        this.cId=cId;
        this.pNickname=pNickname;
        this.pImg=pImg;
        this.crId=crId;
    }

    public String getcId() { return cId; }
    public void setcId(String cId) {this.cId=cId;}
    public String getpNickname() { return pNickname; }
    public void setpNickname(String pNickname) {this.pNickname=pNickname;}
    public String getpImg() { return pImg;}
    public void setpImg(String pImg) { this.pImg=pImg; }
    public String getcrId() { return crId;}
    public void setcrId(String crId) { this.crId=crId; }

}
