package com.example.assignment3.comment;

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
import com.example.assignment3.CommentActivity;
import com.example.assignment3.R;
import com.example.assignment3.models.Comment;
import com.example.assignment3.models.Like;
import com.example.assignment3.models.Post;
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

public class CommentAdapter extends FirestoreRecyclerAdapter<Comment, CommentViewHolder> {
    private static final String TAG = "CommentAdapter";
    private Context context;
    private FirebaseUser currentUser;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private String postId;


    public CommentAdapter(FirestoreRecyclerOptions<Comment> options, Context context,
                          String postId) {
        super(options);
        this.context = context;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.postId = postId;

    }


    @Override
    protected void onBindViewHolder(@NonNull
                                            CommentViewHolder viewHolder, int position,
                                    @NonNull Comment comment) {

        Log.i(TAG, comment.getTextContent());

        //set poster info
        firebaseFirestore.collection(context.getString(R.string.user_collection))
                .document(comment.getUserId()).get().addOnSuccessListener(
                new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User targetUser = documentSnapshot.toObject(User.class);
                        if (targetUser.getImageFileName() != null) {
                            storage.getReference().child("images/" + targetUser.getImageFileName())
                                    .getDownloadUrl()
                                    .addOnSuccessListener(
                                            new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    Glide.with(context).load(uri)
                                                            .into(viewHolder.imagePoster);
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
                                .setText(targetUser.getFullName());
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, e.getMessage());
                ToastMessage(e.getMessage());
            }
        });

        //set content
        viewHolder.textContent.setText(comment.getTextContent());
        if (comment.getTimestamp() != null) {
            viewHolder.commentTimestamp
                    .setText(Utility.calculateDifferentWithCurrentTime(comment.getTimestamp()));
        }

        if (comment.getCommentId() != null) {
            //set listener
            viewHolder.btnLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    firebaseFirestore.collection(context.getString(R.string.post_collection))
                            .document(postId)
                            .collection(context.getString(R.string.comment_collection))
                            .document(comment.getCommentId())
                            .collection(context.getString(R.string.like_collection))
                            .whereEqualTo("userId", currentUser.getUid()).get()
                            .addOnSuccessListener(
                                    new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(
                                                QuerySnapshot queryDocumentSnapshots) {
                                            if (queryDocumentSnapshots.isEmpty()) {
                                                firebaseFirestore
                                                        .collection(context.getString(
                                                                R.string.post_collection))
                                                        .document(postId)
                                                        .collection(
                                                                context.getString(
                                                                        R.string.comment_collection))
                                                        .document(comment.getCommentId())
                                                        .collection(context.getString(
                                                                R.string.like_collection))
                                                        .add(new Like(currentUser.getUid()))
                                                        .addOnSuccessListener(
                                                                new OnSuccessListener<DocumentReference>() {
                                                                    @Override
                                                                    public void onSuccess(
                                                                            DocumentReference documentReference) {
                                                                        Log.e(TAG, "Like Success");
                                                                    }
                                                                })
                                                        .addOnFailureListener(
                                                                new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(
                                                                            @NonNull Exception e) {
                                                                        Log.e(TAG, e.getMessage());
                                                                        ToastMessage(
                                                                                e.getMessage());
                                                                    }
                                                                });
                                            } else {
                                                String targetLikedId =
                                                        queryDocumentSnapshots.getDocuments().get(0)
                                                                .getId();
                                                firebaseFirestore
                                                        .collection(context.getString(
                                                                R.string.post_collection))
                                                        .document(postId)
                                                        .collection(
                                                                context.getString(
                                                                        R.string.comment_collection))
                                                        .document(comment.getCommentId())
                                                        .collection(context.getString(
                                                                R.string.like_collection))
                                                        .document(targetLikedId).delete()
                                                        .addOnSuccessListener(
                                                                new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(
                                                                            Void unused) {
                                                                        Log.e(TAG,
                                                                                "Dislike successfully");
                                                                    }
                                                                }).addOnFailureListener(
                                                        new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(
                                                                    @NonNull Exception e) {
                                                                Log.e(TAG, e.getMessage());
                                                                ToastMessage(e.getMessage());
                                                            }
                                                        });
                                            }
                                        }
                                    });

                }
            });
            //check current user liked post or not
            firebaseFirestore.collection(context.getString(R.string.post_collection))
                    .document(postId)
                    .collection(context.getString(R.string.comment_collection))
                    .document(comment.getCommentId())
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
                    .document(postId)
                    .collection(context.getString(R.string.comment_collection))
                    .document(comment.getCommentId())
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
                                if (value.size() == 0) {
                                    viewHolder.countLike.setVisibility(View.GONE);
                                } else if (value.size() == 1) {
                                    viewHolder.countLike.setText(" • " + value.size() + " Like");
                                } else {
                                    viewHolder.countLike.setText(" • " + value.size() + " Likes");
                                }
                            } else {
                                Log.d(TAG, "Current data: null");
                            }
                        }
                    });
        }


    }


    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup group, int viewType) {
        View view = LayoutInflater.from(group.getContext())
                .inflate(R.layout.item_comment, group, false);
        return new CommentViewHolder(view);
    }


    private void ToastMessage(String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
