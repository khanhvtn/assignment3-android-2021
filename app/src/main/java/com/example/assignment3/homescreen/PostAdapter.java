package com.example.assignment3.homescreen;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.bumptech.glide.Glide;
import com.example.assignment3.CommentActivity;
import com.example.assignment3.R;
import com.example.assignment3.models.Like;
import com.example.assignment3.models.Message;
import com.example.assignment3.models.Post;
import com.example.assignment3.utilities.Utility;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class PostAdapter extends FirestoreRecyclerAdapter<Post, PostViewHolder> {
    private static final String TAG = "PostAdapter";
    private Context context;
    private FirebaseUser currentUser;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();


    public PostAdapter(FirestoreRecyclerOptions<Post> options, Context context) {
        super(options);
        this.context = context;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();

    }


    @Override
    protected void onBindViewHolder(@NonNull
                                            PostViewHolder viewHolder, int position,
                                    @NonNull Post post) {


        //set poster info
        if (post.getPosterImageFileName() != null) {
            storage.getReference().child("images/" + post.getPosterImageFileName()).getDownloadUrl()
                    .addOnSuccessListener(
                            new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(context).load(uri).into(viewHolder.imagePoster);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, e.getMessage());
                    ToastMessage(e.getMessage());
                }
            });
        }
        viewHolder.namePoster
                .setText(post.getPosterName());
        viewHolder.postTimestamp
                .setText(Utility.calculateDifferentWithCurrentTime(post.getTimestamp()));

        //set content
        if (post.getTextContent() != null) {
            //set visible
            viewHolder.textContent.setVisibility(View.VISIBLE);
            viewHolder.textContent.setText(post.getTextContent());
        }
        if (post.getImageContentFileName() != null) {
            //set visible
            viewHolder.imageContentCardView.setVisibility(View.VISIBLE);
            storage.getReference().child("images/" + post.getImageContentFileName())
                    .getDownloadUrl().addOnSuccessListener(
                    new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(context).load(uri).into(viewHolder.imageContent);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, e.getMessage());
                    ToastMessage(e.getMessage());
                }
            });
        }

        //set listener
        viewHolder.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseFirestore.collection(context.getString(R.string.post_collection))
                        .document(post.getPosterId())
                        .collection(context.getString(R.string.like_collection))
                        .whereEqualTo("userId", currentUser.getUid()).get().addOnSuccessListener(
                        new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (queryDocumentSnapshots.isEmpty()) {
                                    firebaseFirestore
                                            .collection(context.getString(R.string.post_collection))
                                            .document(post.getPosterId())
                                            .collection(context.getString(R.string.like_collection))
                                            .add(new Like(currentUser.getUid()))
                                            .addOnSuccessListener(
                                                    new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(
                                                                DocumentReference documentReference) {
                                                            Log.e(TAG, "Like Success");
                                                        }
                                                    })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e(TAG, e.getMessage());
                                                    ToastMessage(e.getMessage());
                                                }
                                            });
                                } else {
                                    String targetLikedId =
                                            queryDocumentSnapshots.getDocuments().get(0).getId();
                                    firebaseFirestore
                                            .collection(context.getString(R.string.post_collection))
                                            .document(post.getPosterId())
                                            .collection(context.getString(R.string.like_collection))
                                            .document(targetLikedId).delete().addOnSuccessListener(
                                            new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Log.e(TAG, "Dislike successfully");
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e(TAG, e.getMessage());
                                            ToastMessage(e.getMessage());
                                        }
                                    });
                                }
                            }
                        });

            }
        });
        viewHolder.btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("postId", post.getPosterId());
                context.startActivity(intent);
            }
        });

        //check current user liked post or not
        firebaseFirestore.collection(context.getString(R.string.post_collection))
                .document(post.getPosterId())
                .collection(context.getString(R.string.like_collection))
                .whereEqualTo("userId", currentUser.getUid()).addSnapshotListener(
                new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        if (value != null) {
                            Log.d(TAG, "Current data: " + value.size());
                            if (value.isEmpty()) {
                                viewHolder.btnLike.setImageDrawable(
                                        context.getDrawable(R.drawable.ic_thumb_up));
                            } else {
                                viewHolder.btnLike.setImageDrawable(
                                        context.getDrawable(R.drawable.ic_thumb_up_liked));
                            }
                        } else {
                            Log.d(TAG, "Current data: null");
                        }
                    }
                });


        //set snapshot listener for like
        firebaseFirestore.collection(context.getString(R.string.post_collection))
                .document(post.getPosterId())
                .collection(context.getString(R.string.like_collection)).addSnapshotListener(
                new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        if (value != null) {
                            Log.d(TAG, "Current data: " + value.size());
                            viewHolder.txtLikeCount.setText(String.valueOf(value.size()));
                        } else {
                            Log.d(TAG, "Current data: null");
                        }
                    }
                });

        //set snapshot listener for comment
        firebaseFirestore.collection(context.getString(R.string.post_collection))
                .document(post.getPosterId())
                .collection(context.getString(R.string.comment_collection)).addSnapshotListener(
                new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        if (value != null) {
                            Log.d(TAG, "Current data: " + value.size());
                            viewHolder.txtCommentCount.setText(String.valueOf(value.size()));
                        } else {
                            Log.d(TAG, "Current data: null");
                        }
                    }
                });

    }


    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup group, int viewType) {
        View view = LayoutInflater.from(group.getContext())
                .inflate(R.layout.item_post, group, false);
        return new PostViewHolder(view);
    }


    private void ToastMessage(String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
