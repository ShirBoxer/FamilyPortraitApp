package com.example.familyportraitapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.familyportraitapp.model.Album;
import com.example.familyportraitapp.model.Model;

import java.util.List;

public class UserAlbumsViewModel extends ViewModel {
    LiveData<List<Album>> UserAlbumsList;

    public UserAlbumsViewModel() {
        this.UserAlbumsList = Model.instance.getAllUserAlbums();
    }

    public LiveData<List<Album>> getUserAlbumsList() {
        return UserAlbumsList;
    }
}
