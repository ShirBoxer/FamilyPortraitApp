package com.example.familyportraitapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class MainFragment extends Fragment {
    Button loginBtn;
    Button registerBtn;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        loginBtn = view.findViewById(R.id.main_f_login);
        registerBtn = view.findViewById(R.id.main_f_register);
        //User logged out & rolled back to main fragment
        if (MainActivity.bottomNavigationView != null)
            MainActivity.bottomNavigationView.setVisibility(View.GONE);
        registerBtn.setOnClickListener((v)-> Navigation.findNavController(view).navigate(R.id.action_mainFragment_to_registerFragment));

        loginBtn.setOnClickListener((v)-> Navigation.findNavController(view).navigate(R.id.action_mainFragment_to_loginFragment));

        return view;
    }
}