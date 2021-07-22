package com.example.familyportraitapp.model;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;

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
    LiveData<List<Album>> allUserAlbums = AppLocalDB.db.albumDao().getAllByOwner(getAuthManager().getCurrentUser().getEmail());




    /* ################################# ---  Interfaces  --- ################################# */

    public interface OnCompleteListener{
        void onComplete();
    }

    public interface GetUserListener{
        void onComplete(User user);
    }

    public interface UploadImageListener{
        void onComplete(String url);
    }


    /* ################################# ---  User CRUD  --- ################################# */

    public void addUser(final User user, final OnCompleteListener listener){
        ModelFirebase.addUser(user, listener);
    }

    public void getUser(final Model.GetUserListener listener){
        ModelFirebase.getUser(listener);
    }

    public void setUserProfileImage(String url, OnCompleteListener listener) {
        ModelFirebase.setUserProfileImage(url, listener);
    }

    public void signOut(){
        ModelFirebase.getFirebaseAuth().signOut();
    }

    public void deleteAccount() {
        ModelFirebase.getFirebaseAuth().getCurrentUser().delete();
    }

    /* ################################# ---  Album CRUD  --- ################################# */

    public LiveData<List<Album>> getAllAlbums(){
        albumsLoadingState.setValue(LoadingState.loading);
        // Read the local last update time
        Long localLastUpdate = Album.getLocalLastUpdateTime();
        // get all updates from firebase
        ModelFirebase.getAllAlbums(localLastUpdate,null, (albums)->{
            executorService.execute(()->{
                Long lastUpdate = new Long(0);
                for(Album a: albums) {
                    // update the local db with the new records
                    AppLocalDB.db.albumDao().insertAll(a);
                    //update the local last update time
                    if (lastUpdate < a.getLastUpdated()){
                        lastUpdate = a.getLastUpdated();
                    }
                }
                Album.setLocalLastUpdateTime(lastUpdate);
                // post => update on the main thread for the observers
                albumsLoadingState.postValue(LoadingState.loaded);
                //read all the data from the local DB already happen while insertion.
                //LiveData gets update automatically
            });
        });
        return allAlbums;
    }

    public LiveData<List<Album>> getAllUserAlbums(){
        userAlbumsLoadingState.setValue(LoadingState.loading);
        // Read the local last update time
        Long localLastUpdate = Album.getLocalLastUpdateTime();
        // get all updates from firebase
        ModelFirebase.getAllAlbums(localLastUpdate,getAuthManager().getCurrentUser().getEmail(),(albums)->{
            executorService.execute(()-> {
                Long lastUpdate = new Long(0);
                for (Album a : albums) {
                    // update the local db with the new records
                    AppLocalDB.db.albumDao().insertAll(a);
                    //update the local last update time
                    if (lastUpdate < a.getLastUpdated()) {
                        lastUpdate = a.getLastUpdated();
                    }
                }
                Album.setLocalLastUpdateTime(lastUpdate);
                // post => update on the main thread for the observers
                userAlbumsLoadingState.postValue(LoadingState.loaded);
                //read all the data from the local DB already happen while insertion.
                //LiveData gets update automatically
            });
        });
        return allUserAlbums;
    }

    public void saveAlbum(Album album, OnCompleteListener listener){
        albumsLoadingState.setValue(LoadingState.loading);
        ModelFirebase.saveAlbum(album, ()->{
            getAllAlbums();
            Log.d("TAG", "HERE");
            Log.d("TAG", album.getDescription());
            listener.onComplete();
        });

    }

    public void deleteAlbum(Album album, OnCompleteListener listener){
        albumsLoadingState.setValue(LoadingState.loading);
        ModelFirebase.deleteAlbum(album, ()->{
            getAllAlbums();
            listener.onComplete();
        });
        executorService.execute(()->{
            AppLocalDB.db.albumDao().delete(album);
        });
        albumsLoadingState.setValue(LoadingState.loaded);
    }

    public void updateAlbum(Album album, OnCompleteListener listener){
        albumsLoadingState.setValue(LoadingState.loading);
        ModelFirebase.saveAlbum(album, ()->{
            getAllAlbums();
            listener.onComplete();
        });
        executorService.execute(()->{
            AppLocalDB.db.albumDao().insertAll(album);
        });

        albumsLoadingState.setValue(LoadingState.loaded);
    }





    /* ################################# ---  Utils  --- ################################# */

    public void uploadImage(Bitmap imageBmp, String name, final UploadImageListener listener) {
        ModelFirebase.uploadImage(imageBmp, name, listener);
    }

    public FirebaseAuth getAuthManager(){
        return ModelFirebase.getFirebaseAuth();
    }


}
