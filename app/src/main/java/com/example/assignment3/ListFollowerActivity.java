package com.example.assignment3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.example.assignment3.listFollower.FollowerAdapter;
import com.example.assignment3.models.Follower;
import com.example.assignment3.models.Message;
import com.example.assignment3.utilities.Utility;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

public class ListFollowerActivity extends AppCompatActivity {
    private AppCompatImageButton btnBack;
    private AppCompatTextView toolbarTitle;
    private RecyclerView rv_user;
    private LinearLayoutManager mLinearLayoutManager;
    private FollowerAdapter followerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_follower);

        //get intent
        String[] arrayInfo = getIntent().getStringArrayExtra("arrayInfo");
        String userId = arrayInfo[0];
        String followType = arrayInfo[1];

        //create linear layout
        mLinearLayoutManager = new LinearLayoutManager(this);

        //declare fields
        btnBack = findViewById(R.id.btnBack);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        rv_user = findViewById(R.id.rv_user);
        rv_user.setLayoutManager(mLinearLayoutManager);

        //change title appbar
        toolbarTitle.setText(followType);
        //set on click listener
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        //create query
        Query query = Utility.firebaseFirestore.collection(getString(R.string.user_collection))
                .document(userId).collection(
                        followType.equals("follower") ? getString(R.string.followers_collection) :
                                getString(R.string.following_collection)).limit(100);

        FirestoreRecyclerOptions<Follower> options =
                new FirestoreRecyclerOptions.Builder<Follower>()
                        .setQuery(query, Follower.class)
                        .setLifecycleOwner(this)
                        .build();
        followerAdapter = new FollowerAdapter(options, this);
        rv_user.setAdapter(followerAdapter);

    }
}