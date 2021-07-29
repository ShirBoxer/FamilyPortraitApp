package com.example.familyportraitapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import android.widget.Toast;

import com.example.familyportraitapp.model.Album;
import com.example.familyportraitapp.model.Model;
import com.example.familyportraitapp.model.MyApplication;
import com.squareup.picasso.Picasso;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static androidx.core.content.ContextCompat.checkSelfPermission;


public class EditAlbumFragment extends Fragment {
    EditText nameTv;
    EditText descriptionTv;
    ImageView albumImageIv;
    Button saveBtn;
    Button deleteBtn;
    FeedViewModel viewModel;
    ImageButton addImgBtn;
    Album album;
    Bitmap imageBitmap = null;
    static final int PERMISSION_CODE = 1001;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_album, container, false);
        nameTv = view.findViewById(R.id.edit_album_f_name_et);
        descriptionTv = view.findViewById(R.id.edit_album_f_description_et);
        albumImageIv = view.findViewById(R.id.edit_album_f_image_iv);
        saveBtn = view.findViewById(R.id.edit_album_f_save_btn);
        addImgBtn = view.findViewById(R.id.edit_album_f_add_pic_btn);
        deleteBtn = view.findViewById(R.id.edit_album_f_delete_btn);

        viewModel = new FeedViewModel();
        String albumId = AlbumFragmentArgs.fromBundle(getArguments()).getAlbumId();

        for(Album a: viewModel.getAlbumsList().getValue()){
            if(a.getId().equals(albumId)){
                album = a;
                break;
            }
        }

        nameTv.setText(album.getName());
        descriptionTv.setText(album.getDescription());
        if(album.getMainPhotoUrl() != null && !album.getMainPhotoUrl().equals(""))
            Picasso.get().load(album.getMainPhotoUrl()).into(albumImageIv);

        addImgBtn.setOnClickListener((v)->{
            LoadCDialogAndImage();
        });

        saveBtn.setOnClickListener((v)->{
            album.setName(nameTv.getText().toString());
            album.setDescription(descriptionTv.getText().toString());
            if(imageBitmap == null)
                Toast.makeText(getContext(), "Please pick an image", Toast.LENGTH_LONG).show();
            else {
                Model.instance.uploadImage(imageBitmap, nameTv.getText().toString(), (uri) -> {
                    album.setMainPhotoUrl(uri);
                    Model.instance.saveAlbum(album, (success) -> {
                        if (success) {
                            Log.d("EDIT_ALBUM", "album" + album.getId() + "edited");
                            Toast.makeText(MyApplication.context, "SUCCESS", Toast.LENGTH_LONG).show();
                            Navigation.findNavController(view).navigateUp();
                        } else {
                            Log.d("EDIT_ALBUM", "album" + album.getId() + "edit was failed");
                            Toast.makeText(MyApplication.context, "Please try again", Toast.LENGTH_LONG).show();
                            Toast.makeText(MyApplication.context, "Please try again", Toast.LENGTH_LONG).show();
                        }

                    });
                });
            }

        });

        deleteBtn.setOnClickListener( (v) -> {
            final CharSequence[] options = {"Yes", "No"};
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Are you sure ??? ");
            builder.setItems(options,(DialogInterface dialog, int item) -> {
                if (options[item].equals("Yes")) {
                    Model.instance.deleteAlbum(album, (success)->{
                        if(success){
                            Log.d("ALBUM", "Album " + album.getId() + "deleted!");
                            Toast.makeText(MyApplication.context, "SUCCESS", Toast.LENGTH_LONG).show();
                            Navigation.findNavController(view).navigate(R.id.action_editAlbumFragment_to_feedFragment);
                        }else{
                            Log.d("ALBUM", "Album " + album.getId() + "Failed on delete");
                            Toast.makeText(MyApplication.context, "Failed, Please try again", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });

            builder.show();
        });
        return view;

    }
    private void LoadCDialogAndImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose your option:");
        builder.setItems(options,(DialogInterface dialog, int item) -> {
            if (options[item].equals("Take Photo")) {
                Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, 0);
            } else if (options[item].equals("Choose from Gallery")) {
                //Ask User for permissions
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        (checkSelfPermission(MyApplication.context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED))
                {
                    //permission not granted, request it
                    String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                    requestPermissions(permissions, PERMISSION_CODE);
                }
                else
                {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, 1);
                }

            } else if (options[item].equals("Cancel"))
                dialog.dismiss();

        });
        builder.show();
    }


    // request code ~ the number of Operation, (REQUEST_IMAGE_CAPTURE=1)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        imageBitmap = (Bitmap) data.getExtras().get("data");
                        albumImageIv.setImageBitmap(imageBitmap);
                        return;
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage =  data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                imageBitmap = BitmapFactory.decodeFile(picturePath);
                                albumImageIv.setImageBitmap(imageBitmap);
                                cursor.close();

                                return;
                            }
                        }
                    }
                    break;
            }
        } else
            Toast.makeText(getContext(), "FAILED",Toast.LENGTH_LONG).show();
    }



}