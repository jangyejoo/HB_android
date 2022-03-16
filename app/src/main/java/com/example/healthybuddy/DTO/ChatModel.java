package com.example.healthybuddy.DTO;

import java.util.HashMap;
import java.util.Map;

public class ChatModel {

    public Map<String, Boolean> users = new HashMap<>();
    public Map<String, Comment> comments = new HashMap<>();

    public static class Comment {
        public String pId;
        public String message;
        public Object timestamp;
    }
}
