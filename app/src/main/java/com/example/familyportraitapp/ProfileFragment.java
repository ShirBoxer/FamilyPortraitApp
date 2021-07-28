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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.familyportraitapp.model.Model;
import com.example.familyportraitapp.model.MyApplication;
import com.example.familyportraitapp.model.User;
import com.squareup.picasso.Picasso;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static androidx.core.content.ContextCompat.checkSelfPermission;
import static com.example.familyportraitapp.AlbumFragment.REQUEST_IMAGE_CAPTURE;


public class ProfileFragment extends Fragment {

    TextView nameTv;
    TextView phoneNumTv;
    Button myAlbumsBtn;
    Button logoutBtn;
    Button deleteAccountBtn;
    ImageView userImgIv;
    ImageButton addImgBtn;
    User user;
    Bitmap imageBitmap;

    static final int PERMISSION_CODE = 1001;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_profile, container, false);

        nameTv = view.findViewById(R.id.profile_f_user_name_tv);
        phoneNumTv = view.findViewById(R.id.profile_f_phone_tv);
        myAlbumsBtn = view.findViewById(R.id.profile_f_albums_btn);
        logoutBtn = view.findViewById(R.id.profile_f_logout_btn);
        deleteAccountBtn = view.findViewById(R.id.profile_f_delete_btn);
        userImgIv = view.findViewById(R.id.profile_f_user_img_iv);
        addImgBtn = view.findViewById(R.id.profile_f_img_btn_btn);

        Model.instance.getUser((newUser)->{
            this.user = newUser;
            nameTv.setText(user.getName());
            phoneNumTv.setText(user.getPhoneNumber());
            Log.d("USER",newUser.getImageUrl());
            if (user.getImageUrl() != null && !user.getImageUrl().equals("")){
                Picasso.get()
                    .load(user.getImageUrl())
                    .placeholder(R.drawable.circle)
                    .error(R.drawable.ic_menu_gallery)
                    .into(userImgIv);
            }

        });

        addImgBtn.setOnClickListener((v)->{
            LoadCDialogAndImage();
        });

        myAlbumsBtn.setOnClickListener((v)->{
            Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_userAlbumsFragment);
        });


        logoutBtn.setOnClickListener((v)->{
            Model.instance.signOut();
            Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_mainFragment);
        });

        deleteAccountBtn.setOnClickListener((v)->{
            Model.instance.deleteAccount();
            Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_mainFragment);

        });

        return view;
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
                        userImgIv.setImageBitmap(imageBitmap);
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
                                userImgIv.setImageBitmap(imageBitmap);
                                cursor.close();
                            }
                        }
                    }
                    break;
            }
            Model.instance.uploadImage(imageBitmap, user.getName() + "_profile_pic",(uri)->{
                this.user.setImageUrl(uri);
                Model.instance.setUserProfileImage(uri, (success -> {
                    if(success){
                        Toast.makeText(MyApplication.context, "SUCCESS", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(MyApplication.context, "FAILED", Toast.LENGTH_LONG).show();

                    }

                }));
            });
        } else
            Toast.makeText(MyApplication.context, "FAILED",Toast.LENGTH_LONG).show();
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

}