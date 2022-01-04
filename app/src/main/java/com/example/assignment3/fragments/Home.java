package com.example.assignment3.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.assignment3.R;
import com.example.assignment3.homescreen.PostAdapter;
import com.example.assignment3.models.Post;
import com.example.assignment3.utilities.Utility;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class Home extends Fragment {
    private final String TAG = "HomeFragment";
    private RecyclerView home_rvPost;
    private LinearLayoutManager mLinearLayoutManager;
    private PostAdapter postAdapter;

    public Home() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //create instance for views
        mLinearLayoutManager = new LinearLayoutManager(getContext());

        //declare field
        home_rvPost = view.findViewById(R.id.home_rvPosts);
        home_rvPost.setLayoutManager(mLinearLayoutManager);

        //create query
        Query query = Utility.firebaseFirestore.collection(getString(R.string.post_collection))
                .orderBy("timestamp").limit(100);

        // Configure recycler adapter options:
        //  * query is the Query object defined above.
        //  * Chat.class instructs the adapter to convert each DocumentSnapshot to a Chat object
        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .setLifecycleOwner(this)
                .build();
        postAdapter = new PostAdapter(options, getContext());
        //set adapter and layout manager for RecyclerView
        home_rvPost.setAdapter(postAdapter);
        return view;
    }
}