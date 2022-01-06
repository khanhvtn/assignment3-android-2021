package com.example.assignment3.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.assignment3.Authentication;
import com.example.assignment3.ChatActivity;
import com.example.assignment3.R;
import com.example.assignment3.homescreen.PostAdapter;
import com.example.assignment3.models.Post;
import com.example.assignment3.models.User;
import com.example.assignment3.utilities.Utility;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import de.hdodenhof.circleimageview.CircleImageView;

public class CurrentUserProfile extends Fragment {
    private final String TAG = "CurrentUserProfile";
    private AppCompatImageButton btnLogout, btnMessages;
    private AppCompatButton btnEditProfile;
    private CircleImageView userAvatar;
    private AppCompatTextView userName, userAddress, userBio, txtCountFollowers, txtCountFollowing;
    private RecyclerView rv_userPost;
    private LinearLayoutManager mLinearLayoutManager;
    private PostAdapter postAdapter;

    public CurrentUserProfile() {
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
        View v = inflater.inflate(R.layout.fragment_current_user_profile, container, false);
        //create linear layout manager.
        mLinearLayoutManager = new LinearLayoutManager(getContext());


        //declare fields
        btnLogout = v.findViewById(R.id.btnLogout);
        btnMessages = v.findViewById(R.id.btnMessages);
        btnEditProfile = v.findViewById(R.id.btnEditProfile);
        userAvatar = v.findViewById(R.id.userAvatar);
        userName = v.findViewById(R.id.userName);
        userAddress = v.findViewById(R.id.userAddress);
        userBio = v.findViewById(R.id.userBio);
        txtCountFollowers = v.findViewById(R.id.txtCountFollowers);
        txtCountFollowing = v.findViewById(R.id.txtCountFollowing);
        rv_userPost = v.findViewById(R.id.rv_userPost);
        rv_userPost.setLayoutManager(mLinearLayoutManager);
        //set user info
        Utility.firebaseFirestore.collection(getString(R.string.user_collection))
                .document(Utility.firebaseAuth.getCurrentUser().getUid()).get().addOnSuccessListener(
                new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User targetUser = documentSnapshot.toObject(User.class);
                        //update user Avatar
                        if (targetUser.getImageFileName() != null) {
                            Utility.firebaseStorage.getReference()
                                    .child("images/" + targetUser.getImageFileName())
                                    .getDownloadUrl().addOnSuccessListener(
                                    new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Glide.with(getContext().getApplicationContext()).load(uri).into(userAvatar);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i(TAG, e.getMessage());
                                    Utility.ToastMessage(e.getMessage(), getContext());
                                }
                            });
                        }
                        //set user name
                        userName.setText(targetUser.getFullName());
                        userAddress.setText(targetUser.getAddress());
                        userBio.setVisibility(View.GONE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, e.getMessage());
                Utility.ToastMessage(e.getMessage(), getContext());
            }
        });

        //set count follower and following
        Utility.firebaseFirestore.collection(getString(R.string.user_collection))
                .document(Utility.firebaseAuth.getCurrentUser().getUid())
                .collection(getString(R.string.followers_collection)).addSnapshotListener(
                new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.i(TAG, e.getMessage());
                            Utility.ToastMessage(e.getMessage(), getContext());
                            return;
                        }
                        if (value != null) {
                            txtCountFollowers.setText(String.valueOf(value.size()));
                        }

                    }
                });
        Utility.firebaseFirestore.collection(getString(R.string.user_collection))
                .document(Utility.firebaseAuth.getCurrentUser().getUid())
                .collection(getString(R.string.following_collection)).addSnapshotListener(
                new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.i(TAG, e.getMessage());
                            Utility.ToastMessage(e.getMessage(), getContext());
                            return;
                        }
                        if (value != null) {
                            txtCountFollowing.setText(String.valueOf(value.size()));
                        }

                    }
                });

        //create query for user post
        Query query = Utility.firebaseFirestore.collection(getString(R.string.post_collection))
                .whereEqualTo("userId", Utility.firebaseAuth.getCurrentUser().getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limitToLast(100);
        FirestoreRecyclerOptions<Post> options =
                new FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post.class)
                        .setLifecycleOwner(this).build();
        //create post adapter
        postAdapter = new PostAdapter(options, getContext());

        //set adapter for Recycler view
        rv_userPost.setAdapter(postAdapter);

        //set lister for button
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getContext(), Authentication.class);
                startActivity(intent);
            }
        });
        btnMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ChatActivity.class);
                startActivity(intent);
            }
        });
        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utility.ToastMessage("Go to Edit Profile Activity", getContext());
            }
        });
        return v;
    }
}