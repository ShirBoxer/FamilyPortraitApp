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


public class UserAlbumsFragment extends Fragment {
    LiveData<List<Album>> userAlbumsList;
    SwipeRefreshLayout swipeRefresh;
    UserAlbumsViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_albums, container, false);
        viewModel = new ViewModelProvider(this).get(UserAlbumsViewModel.class);
        swipeRefresh = view.findViewById(R.id.user_albums_f_swiperefresh);
        setupProgressListener();
        RecyclerView recyclerView = view.findViewById(R.id.user_albums_f_recycler);
        //better performance
        recyclerView.setHasFixedSize(true);


        RecyclerView.LayoutManager manager = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(manager);

        UserAlbumsFragment.MyAdapter adapter = new UserAlbumsFragment.MyAdapter();
        recyclerView.setAdapter(adapter);
        userAlbumsList = viewModel.getUserAlbumsList();


        // from recycler view item to the item (advise fragment)
        adapter.setOnItemClickListener((int position)->{
            String albumId = userAlbumsList.getValue().get(position).getId();
            UserAlbumsFragmentDirections.ActionUserAlbumsFragmentToAlbumFragment action =
                    UserAlbumsFragmentDirections.actionUserAlbumsFragmentToAlbumFragment(albumId);
            Navigation.findNavController(view).navigate(action);
        });



        BottomNavigationView navBar = getActivity().findViewById(R.id.bottom_navigation);
        navBar.setVisibility(View.VISIBLE);
        swipeRefresh.setOnRefreshListener(() -> {
            viewModel.getUserAlbumsList();
            ; //TODO: CREATE REFRESH FUNCTION IN THE VIEWMODEL OBJECT
        });
        // set progressBar and swipeRefresh states
        setupProgressListener();
        // observe liveData object on start and resume
        // notify adapter when posts list arrive
        viewModel.getUserAlbumsList()
                .observe(getViewLifecycleOwner(), (aPList) -> adapter.notifyDataSetChanged());


        return view;
    }

    private void setupProgressListener() {
        Model.instance.userAlbumsLoadingState.observe(getViewLifecycleOwner(), (state) -> {
            switch (state) {
                case loaded:
                    swipeRefresh.setRefreshing(false);
                    break;
                case loading:
                    swipeRefresh.setRefreshing(true);
                    break;
                case error:
                    //TODO: display error message (toast)
            }
        });
    }

    // Breaking inner connection between parent to this class
    // Saving layout components for further use.
    static class MyViewHolder extends RecyclerView.ViewHolder {
        UserAlbumsFragment.OnItemClickListener listener;
        TextView stateTv;
        TextView descriptionTv;
        ImageView picIv;


        public MyViewHolder(@NonNull View itemView, UserAlbumsFragment.OnItemClickListener listener) {
            super(itemView);
            stateTv = itemView.findViewById(R.id.album_list_row_header);
            descriptionTv = itemView.findViewById(R.id.album_list_row_description);
            picIv = itemView.findViewById(R.id.album_list_row_img);
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
            picIv.setImageResource(R.drawable.ic_menu_gallery);
            String url = album.getMainPhotoUrl();
            if ((url != null) && (!url.equals(""))) {
                Picasso.get()
                        .load(url)
                        .placeholder(R.drawable.ic_menu_gallery)
                        .error(R.drawable.ic_menu_gallery)
                        .into(picIv);
            }
        }
    }

    public interface OnItemClickListener{
        void onClick(int position);
    }
    // Managing View logic.
    class MyAdapter extends RecyclerView.Adapter<UserAlbumsFragment.MyViewHolder> {
        UserAlbumsFragment.OnItemClickListener listener;

        public void setOnItemClickListener(UserAlbumsFragment.OnItemClickListener listener){
            this.listener = listener;
        }

        @NonNull
        @Override
        public UserAlbumsFragment.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.album_list_row, parent, false);
            UserAlbumsFragment.MyViewHolder holder = new UserAlbumsFragment.MyViewHolder(view, listener);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull UserAlbumsFragment.MyViewHolder holder, int position) {
            Album album = userAlbumsList.getValue().get(position);
            holder.bind(album);

        }

        // get number of
        @Override
        public int getItemCount() {
            List<Album> sl = userAlbumsList.getValue();
            return (sl == null) ? 0 : sl.size();
        }
    }

}