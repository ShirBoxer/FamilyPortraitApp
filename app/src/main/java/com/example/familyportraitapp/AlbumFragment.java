package com.example.familyportraitapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import com.example.familyportraitapp.model.Album;
import com.example.familyportraitapp.model.Model;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.squareup.picasso.Picasso;

import java.util.Date;

import static android.app.Activity.RESULT_OK;


public class AlbumFragment extends Fragment {
    LiveData<Album> album;
    FeedViewModel viewModel;
    //TODO:
    ProgressBar progressBar;
    SwipeRefreshLayout swipeRefresh;

    TextView headerTv;
    TextView descriptionTv;
    ImageView mainImgIv;
    FloatingActionButton addBtn;
    Button editBtn;
    MyAdapter adapter;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    Bitmap imageBitmap;
    View view;

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
        if(album == null){
            //TOAST
            //LOG
            //BACK TO FEED
            return view;
        }

        RecyclerView recyclerView = view.findViewById(R.id.album_f_recycler);
        //better performance
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager manager = new GridLayoutManager(this.getContext(), 3);


        recyclerView.setLayoutManager(manager);

        adapter = new MyAdapter();
        recyclerView.setAdapter(adapter);
        //album = viewModel.getAlbum();

        //TODO: SET HEADER AND DESCRRIPTION CONTENT
        headerTv.setText(album.getValue().getName());
        descriptionTv.setText(album.getValue().getDescription());
        Picasso.get()
                .load(album.getValue().getMainPhotoUrl())
                .placeholder(R.drawable.ic_menu_gallery)
                .error(R.drawable.ic_menu_gallery)
                .into(mainImgIv);
        if(Model.instance.getAuthManager().getCurrentUser()
                .getEmail().equals(album.getValue().getOwner())) {
            addBtn.setVisibility(View.VISIBLE);
            editBtn.setVisibility(View.VISIBLE);

            addBtn.setOnClickListener((v) -> {
                takePicture();
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

//        swipeRefresh = view.findViewById(R.id.album_f_swiperefresh);
//        swipeRefresh.setOnRefreshListener(() -> {
//            Model.instance.getAllAdvises();
//            ; //TODO: CREATE REFRESH FUNCTION IN THE VIEWMODEL OBJECT
//        });

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
                    //progressBar.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    break;
                case loading:
                    //progressBar.setVisibility(View.VISIBLE);
                    swipeRefresh.setRefreshing(true);
                    break;
                case error:
                    //TODO: display error message (toast)
            }
        });
    }

    private void takePicture(){
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE){
            if (resultCode == RESULT_OK){
                Bundle extras = data.getExtras();
                imageBitmap = (Bitmap) extras.get("data");
                save();
            }
        }


    }

    void save(){
        if(imageBitmap != null){
            Model.instance.uploadImage(imageBitmap, new Timestamp(new Date()).toString(), (url) ->{
                if(url != null){
                    album.getValue().getPhotosUrlList().add(url);
                    Model.instance.saveAlbum(album.getValue(), ()->{
                        this.adapter.notifyDataSetChanged();
                    });

                }
                else return; //TODO: show error to the user
            });
        }
        else
            return; //TODO: show error to the user
        //Navigation.findNavController(view).navigateUp();
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
            Log.d("onBindViewHolder", imageUrl);
            holder.bind(imageUrl);

        }

        // get number of
        @Override
        public int getItemCount() {
            Log.d("getItemCount", album.getValue().getPhotosUrlList().size() + "");
            return album.getValue().getPhotosUrlList().size();
        }
    }







}