package com.example.familyportraitapp.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AlbumDao {
    @Query("select * from Album WHERE isDeleted=0 ORDER BY lastUpdated DESC")
    LiveData<List<Album>> getAll();

    @Query("SELECT * FROM Album WHERE isDeleted=0 AND owner LIKE :filter ORDER BY lastUpdated DESC")
    LiveData<List<Album>> getAllByOwner(String filter);

    //if id already exist replace her.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Album... albums);

    @Delete
    void delete(Album album);


}
