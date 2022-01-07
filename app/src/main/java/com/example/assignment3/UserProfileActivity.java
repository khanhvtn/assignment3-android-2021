package com.example.assignment3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;

import com.bumptech.glide.Glide;
import com.example.assignment3.homescreen.PostAdapter;
import com.example.assignment3.models.Follower;
import com.example.assignment3.models.NotificationApp;
import com.example.assignment3.models.Post;
import com.example.assignment3.models.User;
import com.example.assignment3.utilities.Utility;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {
    private final String TAG = "UserProfileActivity";
    private AppCompatImageButton btnBack, btnMessages;
    private AppCompatButton btnFollow;
    private CircleImageView userAvatar;
    private AppCompatTextView userName, userAddress, userBio, txtCountFollowers, txtCountFollowing;
    private RecyclerView rv_userPost;
    private LinearLayoutManager mLinearLayoutManager;
    private PostAdapter postAdapter;
    private User userProfile, currentUserProfile;
    private ScrollView mainScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //get target user id
        String userId = getIntent().getStringExtra("userId");


        //create linear layout manager.
        mLinearLayoutManager = new LinearLayoutManager(this);


        //declare fields
        btnBack = findViewById(R.id.btnBack);
        btnMessages = findViewById(R.id.btnMessages);
        btnFollow = findViewById(R.id.btnFollow);
        userAvatar = findViewById(R.id.userAvatar);
        userName = findViewById(R.id.userName);
        userAddress = findViewById(R.id.userAddress);
        userBio = findViewById(R.id.userBio);
        txtCountFollowers = findViewById(R.id.txtCountFollowers);
        txtCountFollowing = findViewById(R.id.txtCountFollowing);
        mainScrollView = findViewById(R.id.mainScrollView);
        rv_userPost = findViewById(R.id.rv_userPost);
        rv_userPost.setLayoutManager(mLinearLayoutManager);
        //get current user info
        Utility.firebaseFirestore.collection(getString(R.string.user_collection))
                .document(Utility.firebaseAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(
                        new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                User targetUser = documentSnapshot.toObject(User.class);
                                currentUserProfile = targetUser;
                            }
                        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, e.getMessage());
                Utility.ToastMessage(e.getMessage(), getBaseContext());
            }
        });
        //set user info
        Utility.firebaseFirestore.collection(getString(R.string.user_collection))
                .document(userId).get().addOnSuccessListener(
                new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User targetUser = documentSnapshot.toObject(User.class);
                        userProfile = targetUser;
                        //update user Avatar
                        if (targetUser.getImageFileName() != null) {
                            Utility.firebaseStorage.getReference()
                                    .child("images/" + targetUser.getImageFileName())
                                    .getDownloadUrl().addOnSuccessListener(
                                    new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Glide.with(getApplicationContext()).load(uri)
                                                    .into(userAvatar);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i(TAG, e.getMessage());
                                    Utility.ToastMessage(e.getMessage(), getBaseContext());
                                }
                            });
                        }
                        //set user name
                        userName.setText(targetUser.getFullName());
                        userAddress.setText(targetUser.getAddress());
                        if (!targetUser.getBio().isEmpty()) {
                            userBio.setVisibility(View.VISIBLE);
                            userBio.setText(targetUser.getBio());
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, e.getMessage());
                Utility.ToastMessage(e.getMessage(), getBaseContext());
            }
        });

        //set count follower and following
        Utility.firebaseFirestore.collection(getString(R.string.user_collection))
                .document(userId)
                .collection(getString(R.string.followers_collection)).addSnapshotListener(
                new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.i(TAG, e.getMessage());
                            Utility.ToastMessage(e.getMessage(), getBaseContext());
                            return;
                        }
                        if (value != null) {
                            txtCountFollowers.setText(String.valueOf(value.size()));
                        }

                    }
                });
        Utility.firebaseFirestore.collection(getString(R.string.user_collection))
                .document(userId)
                .collection(getString(R.string.following_collection)).addSnapshotListener(
                new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.i(TAG, e.getMessage());
                            Utility.ToastMessage(e.getMessage(), getBaseContext());
                            return;
                        }
                        if (value != null) {
                            txtCountFollowing.setText(String.valueOf(value.size()));
                        }

                    }
                });

        //create query for user post
        Query query = Utility.firebaseFirestore.collection(getString(R.string.post_collection))
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limitToLast(100);
        FirestoreRecyclerOptions<Post> options =
                new FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post.class)
                        .setLifecycleOwner(this).build();
        //create post adapter
        postAdapter = new PostAdapter(options, getBaseContext());

        //set adapter for Recycler view
        rv_userPost.setAdapter(postAdapter);
        mainScrollView.scrollTo(0, 0);

        //set lister for button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btnMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserProfileActivity.this, ChatActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);

            }
        });
        btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utility.firebaseFirestore.collection(getString(R.string.user_collection))
                        .document(userId).collection(getString(R.string.followers_collection))
                        .document(Utility.firebaseAuth.getUid()).get().addOnSuccessListener(
                        new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Follower follower = documentSnapshot.toObject(Follower.class);
                                if (follower != null) {
                                    //remove follower for viewing user
                                    Utility.firebaseFirestore
                                            .collection(getString(R.string.user_collection))
                                            .document(userId)
                                            .collection(getString(R.string.followers_collection))
                                            .document(Utility.firebaseAuth.getUid())
                                            .delete();
                                    //remove following for current user
                                    Utility.firebaseFirestore
                                            .collection(getString(R.string.user_collection))
                                            .document(Utility.firebaseAuth.getUid())
                                            .collection(getString(R.string.following_collection))
                                            .document(userId).delete();
                                } else {
                                    //add follower for viewing user
                                    Utility.firebaseFirestore
                                            .collection(getString(R.string.user_collection))
                                            .document(userId)
                                            .collection(getString(R.string.followers_collection))
                                            .document(Utility.firebaseAuth.getUid())
                                            .set(new Follower(Utility.firebaseAuth.getUid()));
                                    //add following for current user
                                    Utility.firebaseFirestore
                                            .collection(getString(R.string.user_collection))
                                            .document(Utility.firebaseAuth.getUid())
                                            .collection(getString(R.string.following_collection))
                                            .document(userId).set(new Follower(userId));
                                    //add new notification
                                    Utility.firebaseFirestore
                                            .collection(getString(R.string.user_collection))
                                            .document(userId)
                                            .collection(getString(R.string.notification_collection))
                                            .add(new NotificationApp(
                                                    String.format("%s starts to follow you",
                                                            currentUserProfile.getFullName()),
                                                    getString(R.string.notification_post_type)));
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, e.getMessage());
                        Utility.ToastMessage(e.getMessage(), getBaseContext());
                    }
                });

            }
        });

        //set snapshot listener for follow button
        Utility.firebaseFirestore.collection(getString(R.string.user_collection))
                .document(userId)
                .collection(getString(R.string.followers_collection))
                .whereEqualTo("userId", Utility.firebaseAuth.getUid())
                .addSnapshotListener(
                        new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value,
                                                @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Log.i(TAG, e.getMessage());
                                    return;
                                }
                                btnFollow.setText(value.size() > 0 ? "Unfollow" : "Follow");
                            }
                        });
    }
}