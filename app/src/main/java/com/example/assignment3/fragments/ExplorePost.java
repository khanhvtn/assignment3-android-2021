package com.example.assignment3.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.assignment3.IExploreManagement;
import com.example.assignment3.IMainManagement;
import com.example.assignment3.R;
import com.example.assignment3.homescreen.PostAdapter;
import com.example.assignment3.models.Post;
import com.example.assignment3.utilities.Utility;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

public class ExplorePost extends Fragment {
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView rv_userPost;
    private PostAdapter postAdapter;
    private IMainManagement mainManagement;

    public ExplorePost() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainManagement = (IMainManagement) getActivity();
        //create linear layout manager.
        mLinearLayoutManager = new LinearLayoutManager(getContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_explore_post, container, false);
        rv_userPost = v.findViewById(R.id.rv_userPost);
        rv_userPost.setLayoutManager(mLinearLayoutManager);

        //create query
        Query query = Utility.firebaseFirestore.collection(getString(R.string.post_collection))
                .orderBy("timestamp", Query.Direction.DESCENDING).limitToLast(100);
        // Configure recycler adapter options:
        //  * query is the Query object defined above.
        //  * Chat.class instructs the adapter to convert each DocumentSnapshot to a Chat object
        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .setLifecycleOwner(this)
                .build();
        postAdapter = new PostAdapter(options, getContext(), mainManagement);
        //set adapter and layout manager for RecyclerView
        rv_userPost.setAdapter(postAdapter);
        return v;
    }
}