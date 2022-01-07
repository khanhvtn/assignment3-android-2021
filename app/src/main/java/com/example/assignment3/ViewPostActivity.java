package com.example.assignment3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.assignment3.models.Post;
import com.example.assignment3.post.PostAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ViewPostActivity extends AppCompatActivity {
    private static final String TAG = "View post";
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private PostAdapter postAdapter;
    private final List<Post> postList = new ArrayList<>();
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_homepage);
        recyclerView = findViewById(R.id.recycler_post_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ViewPostActivity.this));


        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();


        postAdapter = new PostAdapter(ViewPostActivity.this, postList);
        recyclerView.setAdapter(postAdapter);
        showPost();

    }



    private void showPost() {
        postList.clear();
        db = FirebaseFirestore.getInstance();
        db.collection("posts").orderBy("timeStamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore error",error.getMessage());
                    return;
                }

                for (DocumentChange documentChange : value.getDocumentChanges()) {
                    if (documentChange.getType() == DocumentChange.Type.ADDED) {
                        postList.add(documentChange.getDocument().toObject(Post.class));
                    }
                }
                postAdapter.notifyDataSetChanged();
            }

        });

    }

}