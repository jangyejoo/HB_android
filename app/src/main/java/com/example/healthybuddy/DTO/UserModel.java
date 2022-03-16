package com.example.healthybuddy.DTO;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

public class UserModel {
    public String Nickname;
    public String img;
    public String pId;

    public UserModel() {
    }

    public UserModel(String Nickname, String img, String pId) {
        this.Nickname = Nickname;
        this.img = img;
        this.pId = pId;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("Nickname", Nickname);
        result.put("img", img);
        result.put("pId", pId);
        return result;
    }

}
