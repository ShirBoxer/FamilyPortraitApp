package com.example.familyportraitapp.model;

import java.util.HashMap;
import java.util.Map;

public class User {

    private String id;
    private String imageUrl; // User image
    private String name;
    private String phoneNumber;

    public Map<String, Object> toMap(){
        HashMap<String, Object> json = new HashMap<>();
        json.put("id", id);
        json.put("name", name);
        json.put("phone", phoneNumber);
        json.put("imageUrl", imageUrl);
        return json;
    }
    public void fromMap(Map<String, Object> map) {
        this.id = (String) map.get("id");
        this.name = (String) map.get("name");
        this.phoneNumber = (String) map.get("phone");
        this.imageUrl = (String) map.get("imageUrl");
    }

    public String getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}

