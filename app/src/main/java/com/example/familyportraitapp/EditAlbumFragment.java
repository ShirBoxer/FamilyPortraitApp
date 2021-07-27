package com.example.familyportraitapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.familyportraitapp.model.Album;
import com.example.familyportraitapp.model.Model;
import com.squareup.picasso.Picasso;

import static android.app.Activity.RESULT_OK;
import static com.example.familyportraitapp.CreateAlbumFragment.REQUEST_IMAGE_CAPTURE;


public class EditAlbumFragment extends Fragment {
    EditText headerTv;
    EditText descriptionTv;
    ImageView albumImageIv;
    Button saveBtn;
    FeedViewModel viewModel;
    ImageButton addImgBtn;
    Album album;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_album, container, false);
        headerTv = view.findViewById(R.id.edit_album_f_header_et);
        descriptionTv = view.findViewById(R.id.edit_album_f_description_et);
        albumImageIv = view.findViewById(R.id.edit_album_f_image_iv);
        saveBtn = view.findViewById(R.id.edit_album_f_save_btn);
        addImgBtn = view.findViewById(R.id.edit_album_f_add_pic_btn);
        viewModel = new FeedViewModel();
        String albumId = AlbumFragmentArgs.fromBundle(getArguments()).getAlbumId();

        for(Album a: viewModel.getAlbumsList().getValue()){
            if(a.getId().equals(albumId)){
                album = a;
                break;
            }
        }

        if (album == null){
            //TOAST
            //LOG
            //BACK TO FEED
            return view;
        }

        headerTv.setText(album.getName());
        descriptionTv.setText(album.getDescription());
        Picasso.get().load(album.getMainPhotoUrl()).into(albumImageIv);

        addImgBtn.setOnClickListener((v)->{
            takePicture();
        });

        saveBtn.setOnClickListener((v)->{
            album.setName(headerTv.getText().toString());
            album.setDescription(descriptionTv.getText().toString());
            Model.instance.saveAlbum(album, ()->{
                Log.d("EDIT_ALBUM", "album" + album.getId()+ "edited");
            });
            Navigation.findNavController(view).navigateUp();
        });

        return view;

    }

    private void takePicture(){
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
    }

    // request code ~ the number of Operation, (REQUEST_IMAGE_CAPTURE=1)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE){
            if (resultCode == RESULT_OK){
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                albumImageIv.setImageBitmap(imageBitmap);
                Model.instance.uploadImage(imageBitmap, album.getId() + "_album_main_img" , (url)->{
                    this.album.setMainPhotoUrl(url);
                });
            }
        }


    }
}