package com.example.assignment3.listFollower;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.assignment3.R;
import com.example.assignment3.UserProfileActivity;
import com.example.assignment3.models.Comment;
import com.example.assignment3.models.Follower;
import com.example.assignment3.models.Like;
import com.example.assignment3.models.User;
import com.example.assignment3.utilities.Utility;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

public class FollowerAdapter extends FirestoreRecyclerAdapter<Follower, FollowerViewHolder> {
    private static final String TAG = "FollowerAdapter";
    private Context context;


    public FollowerAdapter(FirestoreRecyclerOptions<Follower> options, Context context) {
        super(options);
        this.context = context;

    }


    @Override
    protected void onBindViewHolder(@NonNull
                                            FollowerViewHolder viewHolder, int position,
                                    @NonNull Follower follower) {
        Utility.firebaseFirestore.collection(context.getString(R.string.user_collection))
                .document(follower.getUserId()).get().addOnSuccessListener(
                new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user.getImageFileName() != null) {
                            Utility.firebaseStorage.getReference()
                                    .child("images/" + user.getImageFileName()).getDownloadUrl()
                                    .addOnSuccessListener(
                                            new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    Glide.with(context.getApplicationContext())
                                                            .load(uri)
                                                            .into(viewHolder.imageUserAdapter);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, e.getMessage());
                                }
                            });
                        }
                        viewHolder.usernameUserAdapter.setText(user.getFullName());
                        //set on click listener
                        viewHolder.layoutWrapper.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(context, UserProfileActivity.class);
                                intent.putExtra("userId", user.getUserId());
                                context.startActivity(intent);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, e.getMessage());
            }
        });
    }


    @NonNull
    @Override
    public FollowerViewHolder onCreateViewHolder(@NonNull ViewGroup group, int viewType) {
        View view = LayoutInflater.from(group.getContext())
                .inflate(R.layout.item_user, group, false);
        return new FollowerViewHolder(view);
    }


    private void ToastMessage(String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
