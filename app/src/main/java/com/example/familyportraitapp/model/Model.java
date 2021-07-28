package com.example.familyportraitapp.model;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import static androidx.core.content.ContextCompat.checkSelfPermission;


import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Model {
    public static final Model instance = new Model();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private Model() {}




    public enum LoadingState {
        loading,
        loaded,
        error
    }

    /* Data Members & Interfaces */
    public MutableLiveData<LoadingState> albumsLoadingState = new MutableLiveData<LoadingState>(LoadingState.loaded);
    LiveData<List<Album>> allAlbums = AppLocalDB.db.albumDao().getAll();

    public MutableLiveData<LoadingState> userAlbumsLoadingState = new MutableLiveData<LoadingState>(LoadingState.loaded);
    LiveData<List<Album>> allUserAlbums = null;



    /* ################################# ---  Interfaces  --- ################################# */

    public interface OnCompleteListener{
        void onComplete(boolean success);
    }

    public interface GetUserListener{
        void onComplete(User user);
    }

    public interface UploadImageListener{
        void onComplete(String url);
    }


    /* ################################# ---  User CRUD  --- ################################# */

    public void createUser(User user,String password, OnCompleteListener listener){
        ModelFirebase.createUser(user,password, listener);
    }

    public void getUser(final Model.GetUserListener listener){
        ModelFirebase.getUser(listener);
    }

    public void setUserProfileImage(String url, OnCompleteListener listener) {
        ModelFirebase.setUserProfileImage(url, listener);
    }

    public void logIn(String mail, String password, OnCompleteListener listener ) {
        ModelFirebase.logIn(mail, password, listener);
    }

    public void signOut(){
        ModelFirebase.signOut();
    }

    public void resetPassword(String mail, OnCompleteListener listener){
        ModelFirebase.resetPassword(mail, listener);
    }

    public void deleteAccount() {
        ModelFirebase.delete();
    }

    public boolean isCurrentUser(String userId){
        return userId.equals(ModelFirebase.getCurrentUser().getEmail());
    }

    /* ################################# ---  Album CRUD  --- ################################# */

    public interface OnGetAlbumsComplete{
        void onComplete(LiveData<List<Album>> albums);
    }

    public void updateDB(LiveData<List<Album>> albums, MutableLiveData<LoadingState> ls){
        executorService.execute(()->{
            Long lastUpdate = new Long(0);
            for(Album a: albums.getValue()) {
                if(a.getDeleted() == 1)
                    continue;
                // update the local db with the new records
                AppLocalDB.db.albumDao().insertAll(a);
                //update the local last update time
                if (lastUpdate < a.getLastUpdated()){
                    lastUpdate = a.getLastUpdated();
                }
            }
            if (ls == albumsLoadingState)// feed request for albums
                Album.setLocalLastUpdateTime(lastUpdate);
            // post => update on the main thread for the observers
            ls.postValue(LoadingState.loaded);
            //read all the data from the local DB already happen while insertion.
            //LiveData gets update automatically
        });
    }

    public LiveData<List<Album>> getAllAlbums(OnGetAlbumsComplete listener){
        albumsLoadingState.setValue(LoadingState.loading);
        // Read the local last update time
        Long localLastUpdate = Album.getLocalLastUpdateTime();
        ModelFirebase.getAllAlbums(localLastUpdate,(albums)->{
            if(albums != null && albums.getValue() != null){
                updateDB(albums, albumsLoadingState);
                listener.onComplete(allAlbums);
                Log.d("ALBUM","Success on retrieving all albums");
            }

        });
        return allAlbums;
    }

    public LiveData<List<Album>> getAllUserAlbums(OnGetAlbumsComplete listener){
        allUserAlbums = AppLocalDB.db.albumDao().getAllByOwner(ModelFirebase.getCurrentUser().getEmail());
        userAlbumsLoadingState.setValue(LoadingState.loading);
        // Read the local last update time
        Long localLastUpdate = Album.getLocalLastUpdateTime();
        ModelFirebase.getAllUserAlbums(localLastUpdate, (albums)->{
            if(albums != null && albums.getValue() != null){
                updateDB(albums, userAlbumsLoadingState);
                Log.d("ALBUM","Success on retrieving all user albums");

            }

            listener.onComplete(allUserAlbums);

        });
        userAlbumsLoadingState.setValue(LoadingState.loaded);
        return allUserAlbums;
    }

    public void saveAlbum(Album album, OnCompleteListener listener){
        albumsLoadingState.setValue(LoadingState.loading);
        ModelFirebase.saveAlbum(album, (success)->{
            if(success){
                getAllAlbums((albums)->{});
            }
            listener.onComplete(success);
            albumsLoadingState.setValue(LoadingState.loaded);

        });

    }

    public void deleteUserAlbums( OnCompleteListener listener){
        ModelFirebase.deleteUserAlbums((albums) -> {
            if(albums != null){
                executorService.execute(()->{
                    for(int i = 0; i < albums.getValue().size(); i++)
                        AppLocalDB.db.albumDao().delete(albums.getValue().get(i));
                });
            }
            else{
                listener.onComplete(false);
            }
        });
    }

    public void deleteAlbum(Album album, OnCompleteListener listener){
        album.setDeleted(new Long(1));
        albumsLoadingState.setValue(LoadingState.loading);
        ModelFirebase.saveAlbum(album, (success)->{
            if(success){
                getAllAlbums((albums)->{});
                executorService.execute(()->{
                    AppLocalDB.db.albumDao().delete(album);
                });
            }
            listener.onComplete(success);
            albumsLoadingState.setValue(LoadingState.loaded);
        });

    }

    /* ################################# ---  Utils  --- ################################# */

    public void uploadImage(Bitmap imageBmp, String name, final UploadImageListener listener) {
        ModelFirebase.uploadImage(imageBmp, name, listener);
    }




}
