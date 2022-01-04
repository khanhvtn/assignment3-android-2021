package com.example.assignment3;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.assignment3.models.Post;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        Button btnDiscard = findViewById(R.id.post_btnDiscard);
        Button btnPublish = findViewById(R.id.post_btnPublish);
        EditText content = findViewById(R.id.post_content);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        btnDiscard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contentData = content.getText().toString();
                Post post = new Post(contentData, currentUser.getUid());
                post.setReaction(0L);
                if (validatePost(post)) {
                    DocumentReference documentReference = db.collection("posts").document();
                    documentReference.set(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(v.getContext(),"Post successfully!",Toast.LENGTH_SHORT).show();
                        }
                    });

                    finish();
                } else {
                    Toast.makeText(v.getContext(),"Post is empty!",Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private boolean validatePost(Post post) {
        if (!post.getContent().isEmpty()) {
            return true;
        }
        return false;
    };



}