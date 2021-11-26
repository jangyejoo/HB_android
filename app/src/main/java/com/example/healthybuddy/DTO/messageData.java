package com.example.healthybuddy.DTO;

public class messageData {
    //너 사진 너 닉네임 각각 대화내용과 시간
    public String pNickname;
    public String pImg;
    public String mgSender;
    public String mgDetail;
    public String mgDate;

    public int viewType;
/*
    public messageData(String pNickname, String pImg, String mgSender, String mgDetail, String mgDate){
        this.pNickname=pNickname;
        this.pImg=pImg;
        this.mgSender=mgSender;
        this.mgDetail=mgDetail;
        this.mgDate=mgDate;
    }
     */

    public String getpNickname() {return pNickname;}
    public String getpImg() {return pImg;}
    public String getMgSender() {return mgSender;}
    public String getMgDetail() {return mgDetail;}
    public String getMgDate() {return mgDate;}
    public int getViewType() {return viewType;}
}
