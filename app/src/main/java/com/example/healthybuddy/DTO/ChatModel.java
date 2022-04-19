package com.example.healthybuddy.DTO;

import com.example.healthybuddy.Chat;

import java.util.HashMap;
import java.util.Map;

public class ChatModel implements Comparable<ChatModel> {

    public Map<String, Boolean> users = new HashMap<>();
    public Map<String, Comment> comments = new HashMap<>();
    public Map<String, Object> recentTime = new HashMap<>();
    public Map<String, Object> key = new HashMap<>();
    public Map<String, Object> calendar = new HashMap<>();

    @Override
    public int compareTo(ChatModel chatModel) {
        if ((long)chatModel.recentTime.get("recentTime") < (long)this.recentTime.get("recentTime")) {
            return -1;
        } else if ((long)chatModel.recentTime.get("recentTime") == (long)this.recentTime.get("recentTime")) {
            return 0;
        } else {
            return 1;
        }
    }

    public static class Comment {
        public String pId;
        public String message;
        public Object timestamp;
        public Map<String, Object> readUsers = new HashMap<>();
        public String imgUrl;
    }

}
