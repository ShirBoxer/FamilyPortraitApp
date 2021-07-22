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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.example.familyportraitapp.model.Album;
import com.example.familyportraitapp.model.Model;
import com.example.familyportraitapp.model.MyApplication;

import java.util.LinkedList;

import static android.app.Activity.RESULT_OK;


public class CreateAlbumFragment extends Fragment {
    ImageView PictureIv ;
    Button addAlbumBtn;
    EditText nameEt;
    EditText descriptionEt;
    Button createBtn;
    ProgressBar pb;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    Bitmap imageBitmap;
    Spinner spinner;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_create_album, container, false);
        PictureIv = view.findViewById(R.id.create_album_f_img_iv);
        nameEt = view.findViewById(R.id.create_album_f_name_et);
        descriptionEt = view.findViewById(R.id.create_album_f_description_et);
        createBtn = view.findViewById(R.id.create_album_f_create_btn);
        addAlbumBtn = view.findViewById(R.id.create_album_f_add_btn);
        spinner = view.findViewById(R.id.create_album_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(MyApplication.context,R.array.condition,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                if(item != null){
                    Log.d("ITEM", item.toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        addAlbumBtn.setOnClickListener((v)->{
            takePicture();
        });


        createBtn.setOnClickListener((v)->{
            if(nameEt.getText().toString().isEmpty()) {
                nameEt.setError("Please Enter your album header");
                return;
            }
            save();

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
                imageBitmap = (Bitmap) extras.get("data");
                PictureIv.setImageBitmap(imageBitmap);
            }
        }


    }

    void save(){
        //pb.setVisibility(View.VISIBLE);
        createBtn.setEnabled(false);
        //addPictureBtn.setEnabled(false);

        if(imageBitmap != null){
            Model.instance.uploadImage(imageBitmap, nameEt.getText().toString(), (url) ->{
                saveAlbum(url);
            });
        }else{
            saveAlbum(null);
        }


    }

    void saveAlbum(String url){
        String name = nameEt.getText().toString().trim();
        String description = descriptionEt.getText().toString().trim();
        String photoUrl;
        String id = System.currentTimeMillis() + "";
        String owner = Model.instance.getAuthManager().getCurrentUser().getEmail();
        if (url == null)
            photoUrl = "";
        else
            photoUrl = url;


        Album album = new Album(id, name, description, new LinkedList<>() ,owner, photoUrl);

        Model.instance.saveAlbum(album, ()->{
            //TODO TOAST
            //TODO: take uid from ModelFirebase!!!!
        });
        Navigation.findNavController(view).navigate(R.id.action_createAlbumFragment_to_feedFragment);

    }



}