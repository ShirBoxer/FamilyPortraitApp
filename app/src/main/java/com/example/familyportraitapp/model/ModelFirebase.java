package com.example.familyportraitapp.model;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.Navigation;

import com.example.familyportraitapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;

public class ModelFirebase {
    // collections
    // Firestore collections
    final static String albumsCollection = "albums";
    final static String usersCollection = "users";

    // Storage collections
    final static String PHOTOS = "photos";


    private ModelFirebase(){} // mono state class ??


    public static FirebaseAuth getAuthManager(){
        return FirebaseAuth.getInstance();
    }

    public static FirebaseUser getCurrentUser(){
        return getAuthManager().getCurrentUser();
    }

    public static FirebaseFirestore getFirestore(){
        return FirebaseFirestore.getInstance();
    }

    /* ################################# ---  User CRUD  --- ################################# */
    public static void createUser(User user,String password, final Model.OnCompleteListener listener ) {
        getAuthManager().createUserWithEmailAndPassword(user.getId(),password)
                .addOnCompleteListener((@NonNull Task<AuthResult> task)->{
                    if(task.isSuccessful()){
                        FirebaseFirestore db = getFirestore();
                        db.collection(usersCollection).document(user.getId())
                                .set(user.toMap())
                                .addOnCompleteListener((aVoid) -> {
                                    listener.onComplete(true);
                                })
                                .addOnFailureListener((@NonNull Exception e)-> {
                                    Log.d("USER", e.getMessage());
                                    listener.onComplete(false);
                                });
                    }
                    else{
                        Log.d("USER", task.getException().getMessage());
                        listener.onComplete(false);
                    }

                });


    }

    public static void getUser(final Model.GetUserListener listener){
        getFirestore().collection(usersCollection).document(getCurrentUser().getEmail())
                .get().addOnCompleteListener((@NonNull Task<DocumentSnapshot> task)->{
            if(task.isSuccessful() && (task.getResult() != null)){
                User user = (new User()).fromMap(task.getResult().getData());
                listener.onComplete(user);
                return;
            }
            listener.onComplete(null);
        });
    }

    public static void setUserProfileImage(String url, Model.OnCompleteListener listener) {
        getFirestore().collection(usersCollection).document(getCurrentUser().getEmail())
                .update("imageUrl", url)
                .addOnCompleteListener((@NonNull Task<Void> task)->{
                    if(task.isSuccessful() && task.getResult() != null){
                        listener.onComplete(true);
                    }else{
                        listener.onComplete(false);
                    }
                });
    }

    public static void logIn(String mail, String password, Model.OnCompleteListener listener ){
        getAuthManager().signInWithEmailAndPassword(mail, password)
                .addOnCompleteListener( task -> {
                    if (task.isSuccessful())
                        listener.onComplete(true);
                    else
                        listener.onComplete(false);

                });
    }

    public static void signOut(){
        getAuthManager().signOut();
    }

    public static void delete(){
        getCurrentUser().delete();
    }

    public static void resetPassword(String mail, final Model.OnCompleteListener listener){
        getAuthManager()
                .sendPasswordResetEmail(mail)
                .addOnSuccessListener((v) -> listener.onComplete(true))
                .addOnFailureListener((v) -> listener.onComplete(false));
    }

    public static void deleteUserAlbums(final Model.OnGetAlbumsComplete listener){
        List<Album> albums = new LinkedList<>();
        FirebaseFirestore db = getFirestore();
        db.collection(albumsCollection)
                .whereEqualTo("owner", getCurrentUser().getEmail())
                .get().addOnCompleteListener((task -> {
            if(task.isSuccessful()){
                for (QueryDocumentSnapshot document : task.getResult()) {
                    albums.add(Album.createAlbum(document.getData()));
                    albums.get(albums.size() - 1).setDeleted(new Long(1));
                    db.collection(albumsCollection).document( albums.get(albums.size() - 1).getId())
                            .set( albums.get(albums.size() - 1).toJson());

                }
                listener.onComplete(new MutableLiveData<>(albums));
            }
            else
                {
                listener.onComplete(null);
            }
        }));
    }

    /* ################################# ---  Album CRUD  --- ################################# */


    public static void getAllAlbums(Long since, Model.OnGetAlbumsComplete listener){
        FirebaseFirestore db = getFirestore();
        String owner = getCurrentUser().getEmail();
        db.collection(albumsCollection)
                .whereGreaterThanOrEqualTo(Album.LAST_UPDATED, new Timestamp(since,0))
                .whereEqualTo("isDeleted", 0)
                .get()
                .addOnCompleteListener((@NonNull Task<QuerySnapshot> task)->{
                        List<Album> list = new LinkedList<Album>();
                        if(task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult())
                                list.add(Album.createAlbum(document.getData()));
                            listener.onComplete(new MutableLiveData<>(list));
                        }
                        else
                            listener.onComplete(null);

                });

    }

    public static void getAllUserAlbums(Long since, Model.OnGetAlbumsComplete listener){
        FirebaseFirestore db = getFirestore();
        String owner = getCurrentUser().getEmail();
        db.collection(albumsCollection)
                .whereGreaterThanOrEqualTo(Album.LAST_UPDATED, new Timestamp(since,0))
                .whereEqualTo(Album.OWNER, owner)
                .whereEqualTo("isDeleted", 0)
                .get()
                .addOnCompleteListener((@NonNull Task<QuerySnapshot> task)->{
                    List<Album> list = new LinkedList<Album>();
                    if(task.isSuccessful()){
                        for (QueryDocumentSnapshot document : task.getResult())
                            list.add(Album.createAlbum(document.getData()));
                        listener.onComplete(new MutableLiveData<>(list));
                    }
                    else
                        listener.onComplete(null);

                });

    }

    public static void saveAlbum(Album album, Model.OnCompleteListener listener) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        album.setOwner(getCurrentUser().getEmail());
        db.collection(albumsCollection).document(album.getId())
                .set(album.toJson())
                .addOnSuccessListener((v) ->{
                    listener.onComplete(true);
                    Log.d("ALBUM", "saveAlbum success");
                })
                .addOnFailureListener((e) ->{
                    listener.onComplete(false);
                    Log.d("ALBUM", "saveAlbum failed");
                });
    }

    /* ################################# ---  Utils  --- ################################# */


    public static void uploadImage(Bitmap imageBmp, String name, final Model.UploadImageListener listener){
        // get firebase storage instance (singleton)
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create the path
        final StorageReference imagesRef = storage.getReference().child(PHOTOS).child(name);
        //
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //Compressing
        imageBmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        // Upload with Task
        UploadTask uploadTask = imagesRef.putBytes(data);
        uploadTask.addOnFailureListener((exception)->listener.onComplete(null))
                .addOnSuccessListener((taskSnapshot)-> {
                    imagesRef.getDownloadUrl().addOnSuccessListener((uri) -> {
                        Uri downloadUrl = uri;
                        listener.onComplete(downloadUrl.toString());
                    });
                });
    }



}

