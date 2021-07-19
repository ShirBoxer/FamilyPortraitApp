package com.example.familyportraitapp.model;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Album.class}, version = 1)
@TypeConverters({Converters.class})
abstract class AppLocalDbRepository extends RoomDatabase {
    public abstract AlbumDao albumDao();
}

public class AppLocalDB{
    final static public AppLocalDbRepository db =
            Room.databaseBuilder(MyApplication.context,
                    AppLocalDbRepository.class,
                    "FamilyPortrait.db")
                    .fallbackToDestructiveMigration()
                    .build();
}
