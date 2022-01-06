package com.example.assignment3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.assignment3.models.Comment;
import com.example.assignment3.models.Post;
import com.example.assignment3.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        Button btnDiscard = findViewById(R.id.post_btnDiscard);
        Button btnPublish = findViewById(R.id.post_btnPublish);
        ImageView profileImg = findViewById(R.id.post_image);
        EditText content = findViewById(R.id.post_content);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();


        DocumentReference reference = db.collection("users").document(currentUser.getUid());
        reference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {


            }
        });

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
                List<Comment> commentList = new ArrayList<>();
                post.setComments(commentList);
                post.setImgUrl("123");
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