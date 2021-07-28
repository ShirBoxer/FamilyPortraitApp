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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.familyportraitapp.model.Album;
import com.example.familyportraitapp.model.Model;
import com.example.familyportraitapp.model.MyApplication;

import java.util.LinkedList;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static androidx.core.content.ContextCompat.checkSelfPermission;


public class CreateAlbumFragment extends Fragment {
    ImageView PictureIv;
    Button addMainImageBtn;
    EditText nameEt;
    EditText descriptionEt;
    Button createBtn;
    ProgressBar pb;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PERMISSION_CODE = 1001;
    Bitmap imageBitmap;
    Spinner spinner;
    String category;
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
        addMainImageBtn = view.findViewById(R.id.create_album_f_add_btn);
        spinner = view.findViewById(R.id.create_album_spinner);
        pb = view.findViewById(R.id.create_album_f_pb);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(MyApplication.context, R.array.condition, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                if (item != null) {
                    category = (String) item;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        addMainImageBtn.setOnClickListener((v) -> {
            LoadCDialogAndImage();
        });

        createBtn.setOnClickListener((v) -> {
            if (nameEt.getText().toString().isEmpty()) {
                nameEt.setError("Please Enter your album name");
                return;
            }
            pb.setVisibility(ProgressBar.VISIBLE);
            save();
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
                        PictureIv.setImageBitmap(imageBitmap);
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
                                PictureIv.setImageBitmap(imageBitmap);
                                cursor.close();
                            }
                        }
                    }
                    break;
                default :
                    break;
            }
        }

    }

    void save(){
        //pb.setVisibility(View.VISIBLE);
        createBtn.setEnabled(false);
        addMainImageBtn.setEnabled(false);
        // if image was added
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
        String description;

        if(category == null ||category.equals("None")) {
            description = "This is --> General <-- Album" + "\n";
            description.trim();
        }
        else
            description ="This is -->" + category + "<-- Album" + "\n" +descriptionEt.getText().toString().trim();
        String photoUrl;
        String id = System.currentTimeMillis() + "";
        if (url == null)
            photoUrl = "";
        else
            photoUrl = url;

        Album album = new Album(id, name, description, new LinkedList<>() ,"", photoUrl);
        Model.instance.saveAlbum(album, (success)->{
            pb.setVisibility(ProgressBar.INVISIBLE);
            if(success){
                Log.d("ALBUM", "Album was saved successfully");
                Toast.makeText(MyApplication.context, "SUCCESS",Toast.LENGTH_SHORT);
                Navigation.findNavController(view).navigate(R.id.action_createAlbumFragment_to_feedFragment);
            }
            else{
                Toast.makeText(MyApplication.context, "Please try again",Toast.LENGTH_SHORT);
                Log.d("ALBUM", "Album saving was failed");
            }
        });
    }
}