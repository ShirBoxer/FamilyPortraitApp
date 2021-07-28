package com.example.familyportraitapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.familyportraitapp.model.Album;
import com.example.familyportraitapp.model.Model;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

import java.util.List;


public class FeedFragment extends Fragment {
    LiveData<List<Album>> albumsList;
    SwipeRefreshLayout swipeRefresh;
    FeedViewModel viewModel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        viewModel = new ViewModelProvider(this).get(FeedViewModel.class);
        // set swipeRefresh states
        setupProgressListener();
        RecyclerView recyclerView = view.findViewById(R.id.feed_f_recycler);
        //better performance
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(manager);

        MyAdapter adapter = new MyAdapter();
        recyclerView.setAdapter(adapter);
        albumsList = viewModel.getAlbumsList();


        //Select row listener
        adapter.setOnItemClickListener((int position)->{
            String albumId = albumsList.getValue().get(position).getId();
            FeedFragmentDirections.ActionFeedFragmentToAlbumFragment action = FeedFragmentDirections.actionFeedFragmentToAlbumFragment(albumId);
            Navigation.findNavController(view).navigate(action);
        });



        BottomNavigationView navBar = getActivity().findViewById(R.id.bottom_navigation);
        navBar.setVisibility(View.VISIBLE);

        swipeRefresh = view.findViewById(R.id.feed_f_swiperefresh);
        swipeRefresh.setOnRefreshListener(() -> {
            Model.instance.getAllAlbums((albums)->{});
        });

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
                    Log.d("PB", "albumsLoadingState WAS ON ERROR STATE");
            }
        });
    }

    // Breaking inner connection between parent to this class
    // Saving layout components for further use.
    static class MyViewHolder extends RecyclerView.ViewHolder {
        OnItemClickListener listener;
        TextView stateTv;
        TextView descriptionTv;
        ImageView albumIv;


        public MyViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            stateTv = itemView.findViewById(R.id.album_list_row_header);
            descriptionTv = itemView.findViewById(R.id.album_list_row_description);
            albumIv = itemView.findViewById(R.id.album_list_row_img);
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

        public void bind(Album album) {
            stateTv.setText(album.getName());
            descriptionTv.setText(album.getDescription());
            albumIv.setImageResource(R.drawable.ic_menu_gallery);
            String url = album.getMainPhotoUrl();
            if ((url != null) && (!url.equals(""))) {
                Picasso.get()
                        .load(url)
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
            View view = getLayoutInflater().inflate(R.layout.album_list_row, parent, false);
            MyViewHolder holder = new MyViewHolder(view, listener);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            Album album = albumsList.getValue().get(position);
            holder.bind(album);

        }

        // get number of
        @Override
        public int getItemCount() {
            List<Album> sl = albumsList.getValue();
            return (sl == null) ? 0 : sl.size();
        }
    }


}