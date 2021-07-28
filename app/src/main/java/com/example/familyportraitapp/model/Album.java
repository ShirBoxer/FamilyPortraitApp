package com.example.familyportraitapp.model;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
public class Album {
    @PrimaryKey
    @NonNull
    String id;
    String name;
    String description;
    String mainPhotoUrl;
    @TypeConverters(Converters.class)
    List<String> photosUrlList; //TODO type converter (to list<String>)
    Long lastUpdated;
    String owner;
    Long isDeleted;

    final static String IS_DELETED = "isDeleted";
    final static String ID = "id";
    final static String NAME = "name";
    final static String DESCRIPTION = "description";
    final static String PHOTOS_URL_LIST = "photosUrlList";
    final static String LAST_UPDATED = "lastUpdated";
    final static String OWNER = "owner";
    public final static String ALBUM_LAST_UPDATED = "AlbumLastUpdated";
    final static String MAIN_PHOTO_URL = "mainPhotoUrl";

    public Album() {
    }

    @Ignore
    public Album(@NonNull String id, String name, String description,  List<String> photosUrlList, String owner, String mainPhotoUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.photosUrlList = photosUrlList;
        this.lastUpdated = System.currentTimeMillis();
        this.owner = owner;
        this.mainPhotoUrl = mainPhotoUrl;
        this.isDeleted = new Long(0);
    }

    static public void setLocalLastUpdateTime(Long timestamp) {
        SharedPreferences.Editor editor = MyApplication.context.getSharedPreferences("TAG", Context.MODE_PRIVATE).edit();
        editor.putLong(ALBUM_LAST_UPDATED, timestamp);
        editor.commit();
    }

    static public Long getLocalLastUpdateTime() {
        return MyApplication
                .context
                .getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE)
                .getLong(ALBUM_LAST_UPDATED, 0);
    }

    public Map<String, Object> toJson() {
        Map<String, Object> json = new HashMap<>();
        json.put(NAME, name);
        json.put(DESCRIPTION, description);
        json.put(PHOTOS_URL_LIST, photosUrlList);
        json.put(ID, id);
        json.put(LAST_UPDATED, FieldValue.serverTimestamp());
        json.put(OWNER, owner);
        json.put(MAIN_PHOTO_URL, mainPhotoUrl);
        json.put(IS_DELETED, isDeleted);
        return json;
    }

    static public Album createAlbum(Map<String, Object> json) {
        Album a = new Album();
        a.setId((String) json.get(ID));
        a.setDescription((String) json.get(DESCRIPTION));
        a.setPhotosUrlList(( List<String>) json.get(PHOTOS_URL_LIST));
        a.setName((String) json.get(NAME));
        a.setOwner((String) json.get(OWNER));
        a.setDeleted((Long) json.get(IS_DELETED));
        a.setMainPhotoUrl((String)json.get(MAIN_PHOTO_URL));
        Timestamp ts = (Timestamp) json.get(LAST_UPDATED);
        if (ts != null)
            a.setLastUpdated(ts.getSeconds());
        else {
            a.setLastUpdated(new Long(0));
        }
        return a;

    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public  List<String> getPhotosUrlList() {
        return photosUrlList;
    }

    public void setPhotosUrlList( List<String> photosUrlList) {
        this.photosUrlList = photosUrlList;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getMainPhotoUrl() {
        return mainPhotoUrl;
    }

    public void setMainPhotoUrl(String mainPhotoUrl) {
        this.mainPhotoUrl = mainPhotoUrl;
    }

    public Long getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Long deleted) {
        isDeleted = deleted;
    }
}