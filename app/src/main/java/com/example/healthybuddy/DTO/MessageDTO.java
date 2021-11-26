package com.example.healthybuddy.DTO;

public class MessageDTO {
    String pNickname;
    String pImg;
    //String pNickname2;
    //String pImg2;
    String mgSender;
    String mgDetail;
    String mgDate;

    public MessageDTO(){}

    public MessageDTO(String pNickname, String pImg, String mgSender, String mgDetail, String mgDate){
        this.pNickname=pNickname;
        //this.pNickname2=pNickname2;
        this.pImg=pImg;
        //this.pImg2=pImg2;
        this.mgDetail=mgDetail;
        this.mgSender=mgSender;
        this.mgDate=mgDate;
    }

    public String getpNickname() {return pNickname;}
    public void setpNickname(String pNickname) {this.pNickname=pNickname;}
    //public String getpNickname2() {return pNickname2;}
    //public void setpNickname2(String pNickname2) {this.pNickname2=pNickname2;}
    public String getpImg() {return pImg;}
    public void setpImg(String pImg){this.pImg=pImg;}
    //public String getpImg2() {return pImg2;}
    //public void setpImg2(String pImg2){this.pImg2=pImg2;}
    public String getMgSender(){return mgSender;}
    public void setMgSender(String mgSender){this.mgSender=mgSender;}
    public String getMgDetail(){return mgDetail;}
    public void setMgDetail(String mgDetail){this.mgDetail=mgDetail;}
    public String getMgDate(){return mgDate;}
    public void setMgDate(String mgDate){this.mgDate=mgDate;}
}
