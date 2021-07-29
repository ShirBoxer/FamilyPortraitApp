package com.example.familyportraitapp;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.familyportraitapp.model.Model;
import com.example.familyportraitapp.model.MyApplication;


public class LoginFragment extends Fragment {
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    Button loginBtn;
    EditText emailEt;
    EditText passwordEt;
    ProgressBar pb;
    TextView registerTv;
    TextView forgotPassTv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        loginBtn = view.findViewById(R.id.login_f_login_btn);
        emailEt = view.findViewById(R.id.login_f_email);
        passwordEt = view.findViewById(R.id.login_f_password);
        pb = view.findViewById(R.id.login_f_pb);
        registerTv = view.findViewById(R.id.loginfrag_register);
        forgotPassTv = view.findViewById(R.id.loginFrag_forgotPassword);

        //User logged out & rolled back to main fragment
        if (MainActivity.bottomNavigationView != null)
            MainActivity.bottomNavigationView.setVisibility(View.GONE);

        registerTv.setOnClickListener((v)->{
            Navigation.findNavController(view).navigateUp();
        });

        loginBtn.setOnClickListener((v)->{
            pb.setVisibility(View.VISIBLE);
            loginBtn.setEnabled(false);
            String email = emailEt.getText().toString().trim();
            String password = passwordEt.getText().toString().trim();
            if(email.isEmpty() || !email.matches(emailPattern)){
                emailEt.setError("Please enter correct email.");
                return;
            }
            if(password.length() <6){
                passwordEt.setError("Please enter the right password.");
                return;
            }
            Model.instance.logIn(email, password, (success) -> {
                pb.setVisibility(ProgressBar.INVISIBLE);
                if(success){
                    CharSequence text = "Logged in Successfully";
                    Log.d("TAG", text.toString());
                    Toast.makeText(MyApplication.context,text , Toast.LENGTH_LONG).show();
                    Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_feedFragment);
                }else{
                    loginBtn.setEnabled(true);
                    Toast.makeText(MyApplication.context, "Please try again", Toast.LENGTH_LONG).show();
                    Log.d("TAG", "Login failed for User : " + email);
                }
            });
        });

        forgotPassTv.setOnClickListener((v)->{
            EditText resetMail = new EditText(v.getContext());
            AlertDialog.Builder passwordRestDialog = new AlertDialog.Builder(v.getContext());
            passwordRestDialog.setTitle("Reset password ?");
            passwordRestDialog.setMessage("Enter Your Email To Receive Reset Link");
            passwordRestDialog.setView(resetMail);

            passwordRestDialog.setPositiveButton("Send",(DialogInterface dialog, int which) -> {
                //extract the email and sent reset link
                String mail = resetMail.getText().toString();
                Model.instance.resetPassword(mail, (success) -> {
                    if (success) {
                        Toast.makeText(MyApplication.context, "Reset Link Sent To Your Email", Toast.LENGTH_LONG).show();
                        Log.d("PASSWORD", "Reset password  success: ");

                    } else {
                        Toast.makeText(MyApplication.context, "Error ! Reset Link did not Sent !", Toast.LENGTH_LONG).show();
                        Log.d("PASSWORD", "Reset password  failed for user: " + mail);
                    }
                });
            });

            passwordRestDialog.setNegativeButton("Cancel",(DialogInterface dialog, int which) -> {
                //close the dialog
                dialog.dismiss();
            });

            passwordRestDialog.create().show();

        });
        return view;
    }
}