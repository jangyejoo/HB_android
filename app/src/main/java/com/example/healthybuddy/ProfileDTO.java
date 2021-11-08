package com.example.healthybuddy;

import android.provider.MediaStore;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ProfileDTO {
    String pId;
    String pNickname;
    String pGym;
    String pAge;
    String pHeight;
    String pWeight;
    int pSex;
    String pRoutine;
    String pDetail;
    String pImg;
    int pOpen;

    public ProfileDTO(){}

    public ProfileDTO(String pId, String pNickname, String pGym, String pAge, String pHeight, String pWeight, int pSex, String pRoutine, String pDetail, String pImg, int pOpen){
        this.pId=pId;
        this.pNickname=pNickname;
        this.pGym=pGym;
        this.pAge=pAge;
        this.pHeight=pHeight;
        this.pWeight=pWeight;
        this.pSex=pSex;
        this.pRoutine=pRoutine;
        this.pDetail=pDetail;
        this.pImg=pImg;
        this.pOpen=pOpen;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getpNickname() {
        return pNickname;
    }

    public void setpNickname(String pNickname) {
        this.pNickname = pNickname;
    }

    public String getpGym() {
        return pGym;
    }

    public void setpGym(String pGym) {
        this.pGym = pGym;
    }

    public String getpAge() {
        return pAge;
    }

    public void setpAge(String pAge) {
        this.pAge = pAge;
    }

    public String getpHeight() {
        return pHeight;
    }

    public void setpHeight(String pHeight) {
        this.pHeight = pHeight;
    }

    public String getpWeight() {
        return pWeight;
    }

    public void setpWeight(String pWeight) {
        this.pWeight = pWeight;
    }

    public int getpSex() {
        return pSex;
    }

    public void setpSex(int pSex) {
        this.pSex = pSex;
    }

    public String getpRoutine() {
        return pRoutine;
    }

    public void setpRoutine(String pRoutine) {
        this.pRoutine = pRoutine;
    }

    public String getpDetail() {
        return pDetail;
    }

    public void setpDetail(String pDetail) {
        this.pDetail = pDetail;
    }

    public String getpImg() {
        return pImg;
    }

    public void setpImg(String pImg) {
        this.pImg = pImg;
    }

    public int getpOpen() {
        return pOpen;
    }

    public void setpOpen(int pOpen) {
        this.pOpen = pOpen;
    }

    @Override
    public String toString() {
        return "ProfileDTO{" +
                "pId='" + pId + '\'' +
                ", pNickname='" + pNickname + '\'' +
                ", pGym='" + pGym + '\'' +
                ", pAge='" + pAge + '\'' +
                ", pHeight='" + pHeight + '\'' +
                ", pWeight='" + pWeight + '\'' +
                ", pSex='" + pSex + '\'' +
                ", pRoutine='" + pRoutine + '\'' +
                ", pDetail='" + pDetail + '\'' +
                ", pImg='" + pImg + '\'' +
                ", pOpen='" + pOpen + '\'' +
                '}';
    }
}
