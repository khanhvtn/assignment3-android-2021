package com.example.assignment3.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.assignment3.IAuthenticationManagement;
import com.example.assignment3.MainActivity;
import com.example.assignment3.R;
import com.example.assignment3.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class Login extends Fragment {
    private Button btnRegister, btnLogin;
    private EditText edtEmail, edtPassword;
    private IAuthenticationManagement listener;
    private FirebaseAuth mAuth;
    private AlertDialog loadingProgress;
    private FirebaseFirestore db;

    public Login() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //generate progress dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(R.layout.loading_progress).setCancelable(false);
        loadingProgress = builder.create();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //get functions from activity container
        listener = (IAuthenticationManagement) getActivity();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        btnRegister = view.findViewById(R.id.login_btnRegister);
        btnLogin = view.findViewById(R.id.login_btnLogin);
        edtEmail = view.findViewById(R.id.login_edtEmail);
        edtPassword = view.findViewById(R.id.login_edtPassword);

        //add listener
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.switchFragmentInMainActivity(new Register());
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgress.show();
                String email = edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();
                String validateResult = validateUserInput();

                if (validateUserInput() != null) {
                    ToastMessage(validateResult);
                    loadingProgress.dismiss();
                } else {
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(
                            new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(getContext(),
                                                MainActivity.class);
                                        startActivity(intent);
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        ToastMessage("Authentication failed");
                                    }
                                    loadingProgress.dismiss();
                                }
                            });
                }


            }
        });
        return view;
    }

    private String validateUserInput() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        if (email.isEmpty()) {
            return "Email can not be blanked.";
        } else if (password.isEmpty()) {
            return "Password can not be blanked.";
        } else {
            return null;
        }
    }
    private void ToastMessage(String message){
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0,0);
        toast.show();
    }
}