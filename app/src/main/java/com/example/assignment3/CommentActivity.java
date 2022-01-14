package com.example.assignment3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.assignment3.comment.CommentAdapter;
import com.example.assignment3.models.Comment;
import com.example.assignment3.models.Like;
import com.example.assignment3.models.Post;
import com.example.assignment3.models.User;
import com.example.assignment3.utilities.Utility;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentActivity extends AppCompatActivity {
    private final String TAG = "CommentActivity";
    private AppCompatImageButton cm_btnBack, cm_btnLikeComment,
            cm_btnImageSendComment, cm_btnLike;
    private CircleImageView cm_imagePoster;
    private AppCompatTextView cm_namePoster, cm_postTimestamp, cm_textContent, cm_txtLikeCount,
            cm_txtCommentCount, cm_titleComment;
    private CardView cm_imageContentCardView;
    private AppCompatImageView cm_imageContent;
    private AppCompatEditText cm_edtComment;
    private String postId;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView cm_rvComment;
    private CommentAdapter commentAdapter;
    private ScrollView mainScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        //create linear
        mLinearLayoutManager = new LinearLayoutManager(this);

        //get intent
        postId = getIntent().getStringExtra("postId");

        //declare fields
        cm_btnBack = findViewById(R.id.cm_btnBack);
        cm_btnLikeComment = findViewById(R.id.cm_btnLikeComment);
        cm_btnImageSendComment = findViewById(R.id.cm_btnImageSendComment);
        cm_btnLike = findViewById(R.id.cm_btnLike);
        cm_imagePoster = findViewById(R.id.cm_imagePoster);
        cm_namePoster = findViewById(R.id.cm_namePoster);
        cm_postTimestamp = findViewById(R.id.cm_postTimestamp);
        cm_textContent = findViewById(R.id.cm_textContent);
        cm_txtLikeCount = findViewById(R.id.cm_txtLikeCount);
        cm_txtCommentCount = findViewById(R.id.cm_txtCommentCount);
        cm_titleComment = findViewById(R.id.cm_titleComment);
        cm_imageContentCardView = findViewById(R.id.cm_imageContentCardView);
        cm_imageContent = findViewById(R.id.cm_imageContent);
        cm_edtComment = findViewById(R.id.cm_edtComment);
        mainScrollView = findViewById(R.id.mainScrollView);
        cm_rvComment = findViewById(R.id.cm_rvComment);
        cm_rvComment.setLayoutManager(mLinearLayoutManager);

        //fill post content
        Utility.firebaseFirestore.collection(getString(R.string.post_collection)).document(postId)
                .get().addOnSuccessListener(
                new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Post targetPost = documentSnapshot.toObject(Post.class);
                        if (targetPost == null){
                            return;
                        }
                        //set poster image
                        Utility.firebaseFirestore.collection(getString(R.string.user_collection))
                                .document(targetPost.getUserId()).get().addOnSuccessListener(
                                new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        User user = documentSnapshot.toObject(User.class);
                                        if (user.getImageFileName() != null) {
                                            Utility.firebaseStorage.getReference()
                                                    .child("images/" + user.getImageFileName())
                                                    .getDownloadUrl()
                                                    .addOnSuccessListener(
                                                            new OnSuccessListener<Uri>() {
                                                                @Override
                                                                public void onSuccess(Uri uri) {
                                                                    Glide.with(
                                                                            getApplicationContext())
                                                                            .load(uri)
                                                                            .into(cm_imagePoster);
                                                                }
                                                            })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(
                                                                @NonNull Exception e) {
                                                            Log.e(TAG, e.getMessage());
                                                            ToastMessage(e.getMessage());
                                                        }
                                                    });
                                        }
                                        cm_namePoster
                                                .setText(user.getFullName());
                                        cm_namePoster
                                                .setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        //not profile
                                                        if (!Utility.firebaseAuth.getCurrentUser()
                                                                .getUid()
                                                                .equals(targetPost.getUserId())) {
                                                            Intent intent =
                                                                    new Intent(
                                                                            getApplicationContext(),
                                                                            UserProfileActivity.class);
                                                            intent.putExtra("userId",
                                                                    documentSnapshot.getId());
                                                            startActivity(intent);
                                                        }

                                                    }
                                                });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, e.getMessage());
                                ToastMessage(e.getMessage());
                            }
                        });

                        cm_postTimestamp.setText(Utility.calculateDifferentWithCurrentTime(
                                targetPost.getTimestamp()));

                        //set post content
                        if (targetPost.getTextContent() != null) {
                            cm_textContent.setVisibility(View.VISIBLE);
                            cm_textContent.setText(targetPost.getTextContent());
                        }
                        if (targetPost.getImageContentFileName() != null) {
                            cm_imageContentCardView.setVisibility(View.VISIBLE);
                            Utility.firebaseStorage.getReference()
                                    .child("images/" + targetPost.getImageContentFileName())
                                    .getDownloadUrl().addOnSuccessListener(
                                    new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Glide.with(getApplicationContext()).load(uri)
                                                    .into(cm_imageContent);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, e.getMessage());
                                    ToastMessage(e.getMessage());
                                }
                            });

                        }

                        //add snapshot listener for like and comment count.
                        //set snapshot listener for like
                        Utility.firebaseFirestore
                                .collection(getBaseContext().getString(R.string.post_collection))
                                .document(targetPost.getPostId())
                                .collection(getBaseContext().getString(R.string.like_collection))
                                .addSnapshotListener(
                                        new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable QuerySnapshot value,
                                                                @Nullable
                                                                        FirebaseFirestoreException e) {
                                                if (e != null) {
                                                    Log.w(TAG, "Listen failed.", e);
                                                    return;
                                                }

                                                if (value != null) {
                                                    Log.d(TAG, "Current data: " + value.size());
                                                    cm_txtLikeCount
                                                            .setText(String.valueOf(value.size()));
                                                } else {
                                                    Log.d(TAG, "Current data: null");
                                                }
                                            }
                                        });

                        //set snapshot listener for comment
                        Utility.firebaseFirestore
                                .collection(getBaseContext().getString(R.string.post_collection))
                                .document(targetPost.getPostId())
                                .collection(getBaseContext().getString(R.string.comment_collection))
                                .addSnapshotListener(
                                        new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable QuerySnapshot value,
                                                                @Nullable
                                                                        FirebaseFirestoreException e) {
                                                if (e != null) {
                                                    Log.w(TAG, "Listen failed.", e);
                                                    return;
                                                }

                                                if (value != null) {
                                                    Log.d(TAG, "Current data: " + value.size());
                                                    cm_txtCommentCount
                                                            .setText(String.valueOf(value.size()));
                                                    cm_titleComment.setText(
                                                            "Comments (" + value.size() + ")");
                                                } else {
                                                    Log.d(TAG, "Current data: null");
                                                }
                                            }
                                        });

                    }


                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, e.getMessage());
                ToastMessage(e.getMessage());
            }
        });

        //button listener
        /**
         * If user do not input any comment. Then the app will display like button, vice versa.
         * */
        cm_edtComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    cm_btnImageSendComment.setVisibility(View.VISIBLE);
                    cm_btnLikeComment.setVisibility(View.GONE);
                } else {
                    cm_btnImageSendComment.setVisibility(View.GONE);
                    cm_btnLikeComment.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        cm_btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        cm_btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utility.firebaseFirestore.collection(getString(R.string.post_collection))
                        .document(postId)
                        .collection(getString(R.string.like_collection))
                        .whereEqualTo("userId", Utility.firebaseAuth.getCurrentUser().getUid())
                        .get()
                        .addOnSuccessListener(
                                new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        if (queryDocumentSnapshots.isEmpty()) {
                                            Utility.firebaseFirestore
                                                    .collection(getString(R.string.post_collection))
                                                    .document(postId)
                                                    .collection(getString(R.string.like_collection))
                                                    .add(new Like(
                                                            Utility.firebaseAuth.getCurrentUser()
                                                                    .getUid()))
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
                                                        public void onFailure(
                                                                @NonNull Exception e) {
                                                            Log.e(TAG, e.getMessage());
                                                            ToastMessage(e.getMessage());
                                                        }
                                                    });
                                        } else {
                                            String targetLikedId =
                                                    queryDocumentSnapshots.getDocuments().get(0)
                                                            .getId();
                                            Utility.firebaseFirestore
                                                    .collection(getString(R.string.post_collection))
                                                    .document(postId)
                                                    .collection(getString(R.string.like_collection))
                                                    .document(targetLikedId).delete()
                                                    .addOnSuccessListener(
                                                            new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    Log.e(TAG,
                                                                            "Dislike successfully");
                                                                }
                                                            })
                                                    .addOnFailureListener(new OnFailureListener() {
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

        cm_btnLikeComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "Send Comment");
                char[] icon = Character.toChars(0x1F44D);
                StringBuilder stringBuilder = new StringBuilder();
                for (char ch : icon
                ) {
                    stringBuilder.append(ch);
                }
                sendComment(stringBuilder.toString());
            }
        });
        cm_btnImageSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendComment(cm_edtComment.getText().toString());
            }
        });

        //create query for comment
        Query query = Utility.firebaseFirestore.collection(getString(R.string.post_collection))
                .document(postId)
                .collection(getString(R.string.comment_collection))
                .orderBy("timestamp", Query.Direction.DESCENDING).limitToLast(100);
        FirestoreRecyclerOptions<Comment> options = new FirestoreRecyclerOptions.Builder<Comment>()
                .setQuery(query, Comment.class)
                .setLifecycleOwner(this)
                .build();
        commentAdapter = new CommentAdapter(options, this, postId);
        //scroll to newest comment
        commentAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                cm_rvComment.scrollToPosition(0);
                mainScrollView.scrollTo(0, 0);
            }
        });

        //set adapter for recycler view
        cm_rvComment.setAdapter(commentAdapter);
        mainScrollView.scrollTo(0, 0);

        //snapshot listener
        //check current user liked post or not
        Utility.firebaseFirestore.collection(getString(R.string.post_collection))
                .document(postId)
                .collection(getString(R.string.like_collection))
                .whereEqualTo("userId", Utility.firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(
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
                                        cm_btnLike.setImageDrawable(
                                                getDrawable(R.drawable.ic_thumb_up));
                                    } else {
                                        cm_btnLike.setImageDrawable(
                                                getDrawable(R.drawable.ic_thumb_up_liked));
                                    }
                                } else {
                                    Log.d(TAG, "Current data: null");
                                }
                            }
                        });

    }

    private void ToastMessage(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void sendComment(String textContent) {
        Comment newComment = new
                Comment(FirebaseAuth.getInstance().getUid(), textContent);

        Utility.firebaseFirestore.collection(getString(R.string.post_collection)).document(postId)
                .collection(getString(R.string.comment_collection)).add(newComment)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "New Comment added with ID: " + documentReference.getId());
                        newComment.setCommentId(documentReference.getId());
                        documentReference.set(newComment, SetOptions.merge());
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error adding document", e);
            }
        });
        cm_edtComment.setText("");
    }
}