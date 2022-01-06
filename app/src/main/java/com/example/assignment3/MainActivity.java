package com.example.assignment3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, Authentication.class));
        }

        AppCompatButton btnLogout = findViewById(R.id.main_btnLogout);
        AppCompatButton btnChatRoom = findViewById(R.id.main_btnChatRoom);
        AppCompatButton btnCreatePost = findViewById(R.id.main_btnCreatePost);
        AppCompatButton btnViewPost = findViewById(R.id.main_btnViewPost);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(view.getContext(), Authentication.class));
            }
        });
        btnChatRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), ChatActivity.class));
            }
        });
        btnCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), PostActivity.class));
            }
        });
        btnViewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), ViewPostActivity.class));
            }
        });

    }
}