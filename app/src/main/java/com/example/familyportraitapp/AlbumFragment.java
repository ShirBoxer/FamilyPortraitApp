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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.familyportraitapp.model.Album;
import com.example.familyportraitapp.model.Model;
import com.example.familyportraitapp.model.MyApplication;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.squareup.picasso.Picasso;

import java.util.Date;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static androidx.core.content.ContextCompat.checkSelfPermission;


public class AlbumFragment extends Fragment {
    LiveData<Album> album;
    FeedViewModel viewModel;
    SwipeRefreshLayout swipeRefresh;
    TextView headerTv;
    TextView descriptionTv;
    ImageView mainImgIv;
    FloatingActionButton addBtn;
    Button editBtn;
    MyAdapter adapter;
    Bitmap imageBitmap;
    View view;

    static final int PERMISSION_CODE = 1001;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_album, container, false);

        String albumId = AlbumFragmentArgs.fromBundle(getArguments()).getAlbumId();

        viewModel = new ViewModelProvider(this).get(FeedViewModel.class);
        headerTv = view.findViewById(R.id.album_f_header_tv);
        descriptionTv = view.findViewById(R.id.album_f_description_tv);
        mainImgIv = view.findViewById(R.id.album_f_image_iv);
        addBtn = view.findViewById(R.id.album_f_add_btn);
        editBtn = view.findViewById(R.id.album_f_edit_btn);
        swipeRefresh = view.findViewById(R.id.album_f_swiperefresh);

        for(Album a: viewModel.getAlbumsList().getValue()){
            if(a.getId().equals(albumId)){
                album = new MutableLiveData(a);
                break;
            }
        }

        RecyclerView recyclerView = view.findViewById(R.id.album_f_recycler);
        //better performance
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager manager = new GridLayoutManager(this.getContext(), 3);

        recyclerView.setLayoutManager(manager);
        adapter = new MyAdapter();
        recyclerView.setAdapter(adapter);

        headerTv.setText(album.getValue().getName());
        descriptionTv.setText(album.getValue().getDescription());
        String mainUrl = album.getValue().getMainPhotoUrl();
        if(mainUrl != null && !mainUrl.equals(""))
            Picasso.get()
                    .load(album.getValue().getMainPhotoUrl())
                    .placeholder(R.drawable.ic_menu_gallery)
                    .error(R.drawable.ic_menu_gallery)
                    .into(mainImgIv);

        if(Model.instance.isCurrentUser(album.getValue().getOwner())) {
            addBtn.setVisibility(View.VISIBLE);
            editBtn.setVisibility(View.VISIBLE);

            addBtn.setOnClickListener((v) -> {
                LoadCDialogAndImage();
            });

            editBtn.setOnClickListener((v) -> {
                AlbumFragmentDirections.ActionAlbumFragmentToEditAlbumFragment action = AlbumFragmentDirections.actionAlbumFragmentToEditAlbumFragment(albumId);
                Navigation.findNavController(view).navigate(action);
            });
        }
        //Select rectangle listener
        adapter.setOnItemClickListener((int position)->{
            String imageUrl = album.getValue().getPhotosUrlList().get(position);
            AlbumFragmentDirections.ActionAlbumFragmentToImageFragment action = AlbumFragmentDirections.actionAlbumFragmentToImageFragment(imageUrl, album.getValue().getOwner());
            Navigation.findNavController(view).navigate(action);
        });

        BottomNavigationView navBar = getActivity().findViewById(R.id.bottom_navigation);
        navBar.setVisibility(View.VISIBLE);

        swipeRefresh.setOnRefreshListener(() -> {
            Model.instance.getAllUserAlbums((success) -> {swipeRefresh.setRefreshing(false);});

        });

        setupProgressListener();
        // observe liveData object on start and resume
        // notify adapter when posts list arrive
        viewModel.getAlbumsList()
                .observe(getViewLifecycleOwner(), (aPList) -> adapter.notifyDataSetChanged());

        return view;
    }

    private void setupProgressListener() {
        Model.instance.albumsLoadingState.observe(getViewLifecycleOwner(), (state) -> {
            switch (state) {
                case loaded:
                    swipeRefresh.setRefreshing(false);
                    break;
                case loading:
                    swipeRefresh.setRefreshing(true);
                    break;
                case error:
                    Log.d("PB", "albumLoadingState WAS ON ERROR STATE");
            }
        });
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
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            if(resultCode != RESULT_CANCELED) {
                switch (requestCode) {
                    case 0:
                        if (resultCode == RESULT_OK && data != null) {
                            imageBitmap = (Bitmap) data.getExtras().get("data");
                            save();
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
                                    cursor.close();
                                    save();
                                    return;
                                }
                            }
                        }
                        break;
                }
            } else
                Toast.makeText(getContext(), "FAILED",Toast.LENGTH_LONG).show();
        }

    void save(){
        if(imageBitmap != null){
            Model.instance.uploadImage(imageBitmap, new Timestamp(new Date()).toString(), (url) ->{
                if(url != null){
                    album.getValue().getPhotosUrlList().add(url);
                    Model.instance.saveAlbum(album.getValue(), (success)->{
                        if(success){
                            this.adapter.notifyDataSetChanged();
                        }else{
                            Toast.makeText(MyApplication.context, "Please try again", Toast.LENGTH_LONG).show();
                        }

                    });
                }
                else{
                    Toast.makeText(getContext(), "Please try again", Toast.LENGTH_LONG).show();
                    return;
                }
            });
        }
        else{
            Toast.makeText(MyApplication.context, "Please try again", Toast.LENGTH_LONG).show();
            return;
        }
        //dshmdNavigation.findNavController(view).navigateUp();
    }


    // Breaking inner connection between parent to this class
    // Saving layout components for further use.
    static class MyViewHolder extends RecyclerView.ViewHolder {
        OnItemClickListener listener;
        ImageView albumIv;

        public MyViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            albumIv = itemView.findViewById(R.id.album_list_square_iv);
            this.listener = listener;
            itemView.setOnClickListener((v)->{
                if(listener != null){
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION){
                        listener.onClick(position);
                    }
                }
            });
        }

        public void bind(String imageUrl) {
            albumIv.setImageResource(R.drawable.ic_menu_gallery);
            if ((imageUrl != null) && (!imageUrl.equals(""))) {
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_menu_gallery)
                        .error(R.drawable.ic_menu_gallery)
                        .into(albumIv);
            }
        }
    }

    public interface OnItemClickListener{
        void onClick(int position);
    }

    // Managing View logic.
    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        OnItemClickListener listener;

        public void setOnItemClickListener(OnItemClickListener listener){
            this.listener = listener;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.album_list_square, parent, false);
            MyViewHolder holder = new MyViewHolder(view, listener);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            String imageUrl =  album.getValue().getPhotosUrlList().get(position);
            holder.bind(imageUrl);

        }

        // get number of
        @Override
        public int getItemCount() {
            return album.getValue().getPhotosUrlList().size();
        }
    }







}