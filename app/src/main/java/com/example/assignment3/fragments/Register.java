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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


public class Register extends Fragment {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Button btnRegister, btnLogin;
    private EditText edtFullName, edtEmail, edtPassword, edtAddress;
    private IAuthenticationManagement listener;
    private AlertDialog loadingProgress;

    public Register() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //generate progress dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(R.layout.loading_progress).setCancelable(false);
        loadingProgress = builder.create();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //get functions from activity container
        listener = (IAuthenticationManagement) getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        //declare field
        edtFullName = view.findViewById(R.id.register_edtFullName);
        edtEmail = view.findViewById(R.id.register_edtEmail);
        edtPassword = view.findViewById(R.id.register_edtPassword);
        edtAddress = view.findViewById(R.id.register_edtAddress);
        btnLogin = view.findViewById(R.id.register_btnLogin);
        btnRegister = view.findViewById(R.id.register_btnRegister);

        //add listener
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.switchFragmentInMainActivity(new Login());
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgress.show();
                String fullName = edtFullName.getText().toString().trim();
                String email = edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();
                String address = edtAddress.getText().toString().trim();
                String validateResult =
                        validateUserInput(fullName, email, password, address);
                if (validateResult != null) {
                    Toast.makeText(v.getContext(), validateResult, Toast.LENGTH_SHORT).show();
                    loadingProgress.dismiss();
                } else {
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
                            new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {
                                        String userId = task.getResult().getUser().getUid();
                                        User newUser = new User(userId, fullName, email, address);
                                        db.collection("users")
                                                .document(userId)
                                                .set(newUser).addOnCompleteListener(
                                                new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(
                                                            @NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            ToastMessage(
                                                                    "Register Successful. Please Login!!!");
                                                            Intent intent = new Intent(getContext(),
                                                                    MainActivity.class);
                                                            startActivity(intent);
                                                        } else {
                                                            ToastMessage(task.getException()
                                                                    .getMessage());
                                                        }
                                                        loadingProgress.dismiss();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(v.getContext(),
                                                task.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                        loadingProgress.dismiss();
                                    }
                                }
                            });
                }
            }
        });

        return view;
    }

    public String validateUserInput(String fullName, String email, String password,
                                    String address) {
        if (fullName.isEmpty()) {
            return "Full name can not be blanked";
        }
        if (email.isEmpty()) {
            return "Email can not be blanked";
        }
        if (password.isEmpty()) {
            return "Password can not be blanked";
        }
        if (address.isEmpty()) {
            return "Address can not be blanked";
        }

        return null;
    }

    private void ToastMessage(String message) {
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}