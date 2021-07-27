package com.example.familyportraitapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.familyportraitapp.model.Album;
import com.example.familyportraitapp.model.Model;

import java.util.List;

public class FeedViewModel extends ViewModel {
    LiveData<List<Album>> AlbumsList;

    public FeedViewModel() {
        this.AlbumsList = Model.instance.getAllAlbums((success) -> {});
    }

    public LiveData<List<Album>> getAlbumsList() {
        return AlbumsList;
    }

}
