package com.example.assignment3.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.example.assignment3.ChatActivity;
import com.example.assignment3.IMainManagement;
import com.example.assignment3.R;
import com.example.assignment3.homescreen.PostAdapter;
import com.example.assignment3.models.Follower;
import com.example.assignment3.models.Post;
import com.example.assignment3.utilities.Utility;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.LinkedList;
import java.util.List;

public class Home extends Fragment {
    private final String TAG = "HomeFragment";
    private RecyclerView home_rvPost;
    private LinearLayoutManager mLinearLayoutManager;
    private PostAdapter postAdapter;
    private AppCompatImageButton home_btnMessages;
    private IMainManagement listener;
    private ScrollView mainScrollView;
    private AppCompatTextView noPostTitle;
    private ListenerRegistration registration;

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

        //get listener;
        listener = (IMainManagement) getActivity();

        //declare field
        home_rvPost = view.findViewById(R.id.home_rvPosts);
        home_rvPost.setLayoutManager(mLinearLayoutManager);
        home_btnMessages = view.findViewById(R.id.home_btnMessages);
        mainScrollView = view.findViewById(R.id.mainScrollView);
        noPostTitle = view.findViewById(R.id.noPostTitle);

        //get list following
        Utility.firebaseFirestore.collection(getString(R.string.user_collection))
                .document(Utility.firebaseAuth.getCurrentUser().getUid())
                .collection(getString(R.string.following_collection)).get().addOnSuccessListener(
                new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Follower> followerList =
                                queryDocumentSnapshots.toObjects(Follower.class);
                        List<String> followerIdList = new LinkedList<>();
                        for (Follower follower : followerList
                        ) {
                            followerIdList.add(follower.getUserId());
                        }
                        if (followerIdList.size() == 0) {
                            mainScrollView.setVisibility(View.GONE);
                            noPostTitle.setVisibility(View.VISIBLE);
                        } else {
                            mainScrollView.setVisibility(View.VISIBLE);
                            noPostTitle.setVisibility(View.GONE);
                            //create query
                            Query query = Utility.firebaseFirestore
                                    .collection(getString(R.string.post_collection))
                                    .whereIn("userId", followerIdList)
                                    .orderBy("timestamp", Query.Direction.DESCENDING)
                                    .limitToLast(100);

                            // Configure recycler adapter options:
                            //  * query is the Query object defined above.
                            //  * Chat.class instructs the adapter to convert each DocumentSnapshot to a Chat object
                            FirestoreRecyclerOptions<Post> options =
                                    new FirestoreRecyclerOptions.Builder<Post>()
                                            .setQuery(query, Post.class)
                                            .setLifecycleOwner(getViewLifecycleOwner())
                                            .build();
                            postAdapter =
                                    new PostAdapter(options, getContext(), listener, false, null);
                            //scroll to newest post
                            postAdapter.registerAdapterDataObserver(
                                    new RecyclerView.AdapterDataObserver() {
                                        @Override
                                        public void onItemRangeInserted(int positionStart,
                                                                        int itemCount) {
                                            super.onItemRangeInserted(positionStart, itemCount);
                                            home_rvPost.scrollToPosition(0);
                                            mainScrollView.scrollTo(0, 0);
                                        }
                                    });
                            //set adapter and layout manager for RecyclerView
                            home_rvPost.setAdapter(postAdapter);
                            mainScrollView.scrollTo(0, 0);

                            /**
                             * Add SnapshotListener to get chat rooms in realtime
                             * */
                            registration = query.addSnapshotListener((snapshot, error) -> {
                                if (error != null) {
                                    //Handle error
                                    return;
                                }
                                //Convert query snapshot to a list of follower
                                List<Post> postList =
                                        snapshot.toObjects(Post.class);
                                //Update UI
                                if (postList.isEmpty()) {
                                    mainScrollView.setVisibility(View.GONE);
                                    noPostTitle.setVisibility(View.VISIBLE);
                                } else {
                                    mainScrollView.setVisibility(View.VISIBLE);
                                    noPostTitle.setVisibility(View.GONE);
                                }
                            });
                        }


                        //set listener
                        home_btnMessages.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(getContext(), ChatActivity.class);
                                startActivity(intent);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, e.getMessage());
            }
        });


        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "OnResume");
        home_rvPost.setAdapter(postAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "OnDestroy");
        if(registration != null){
            registration.remove();
        }
    }
}