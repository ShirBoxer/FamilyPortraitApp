package com.example.familyportraitapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.familyportraitapp.model.Model;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;


public class ImageFragment extends Fragment {

   ImageView mainImageIv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_image, container, false);

        String imageUrl = ImageFragmentArgs.fromBundle(getArguments()).getImageUrl();
        String albumOwner = ImageFragmentArgs.fromBundle(getArguments()).getAlbumOwner();

        mainImageIv = view.findViewById(R.id.image_f_iv);


        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.ic_menu_gallery)
                .error(R.drawable.ic_menu_gallery)
                .into(mainImageIv);


        return view;
    }
}