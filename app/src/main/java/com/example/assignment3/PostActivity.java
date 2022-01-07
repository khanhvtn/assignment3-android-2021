package com.example.assignment3;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.assignment3.fragments.CaptureImageDialogFragment;
import com.example.assignment3.models.Comment;
import com.example.assignment3.models.Post;
import com.example.assignment3.models.User;
import com.example.assignment3.utilities.GlideApp;
import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostActivity extends AppCompatActivity {
    private static final int SELECT_PICTURE = 200;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    Button btnDiscard, btnPublish, btnAddImage, btnCapture;
    ImageButton btnAttachment;
    ImageView profileImg, postImg;
    EditText content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        btnDiscard = findViewById(R.id.post_btnDiscard);
        btnPublish = findViewById(R.id.post_btnPublish);
        btnAttachment = findViewById(R.id.post_btnAttachment);
        btnAddImage = findViewById(R.id.post_btn_addImg);
        btnCapture = findViewById(R.id.post_btn_capture);
        profileImg = findViewById(R.id.post_profile);
        content = findViewById(R.id.post_content);
        postImg = findViewById(R.id.post_picture);
        postImg.setDrawingCacheEnabled(true);
        postImg.buildDrawingCache();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        btnAddImage.setVisibility(View.GONE);
        btnCapture.setVisibility(View.GONE);
        StorageReference storageReference = storage.getReference();




        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle bundle = result.getData().getExtras();
                    Bitmap bitmap = (Bitmap) bundle.get("data");
                    postImg.setImageBitmap(bitmap);
                }
            }
        });

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    activityResultLauncher.launch(intent);
                } else {
                    Toast.makeText(PostActivity.this,"No camera app found", Toast.LENGTH_SHORT).show();
                }
            }
        });



        //Load current user profile image
        db.collection("users").document(currentUser.getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                if (user.getImageFileName()!=null) {
                    StorageReference profileImgRef = storageReference.child("images/" + user.getImageFileName());
                    GlideApp.with(PostActivity.this).load(profileImgRef).apply(RequestOptions.circleCropTransform()).into(profileImg);
                }
            }
        });



        //Discard button
        btnDiscard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Add attachment
        btnAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnAddImage.getVisibility() == View.GONE) {
                    btnAddImage.setVisibility(View.VISIBLE);
                    btnCapture.setVisibility(View.VISIBLE);
                } else {
                    btnAddImage.setVisibility(View.GONE);
                    btnCapture.setVisibility(View.GONE);
                }
            }
        });

        //Add image
        btnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //Create post
        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contentData = content.getText().toString();
                Post post = new Post(contentData, currentUser.getUid());
                post.setReaction(0L);
                List<Comment> commentList = new ArrayList<>();
                post.setComments(commentList);
                
                //Check if there is an image to upload
                if (validatePost(post)) {
                    DocumentReference documentReference = db.collection("posts").document();
                    Bitmap bitmap = ((BitmapDrawable) postImg.getDrawable()).getBitmap();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                    byte[] data = baos.toByteArray();
                    String uploadName = UUID.randomUUID().toString() + ".jpg";
                    post.setImgUrl("images/" + uploadName);
                    StorageReference upload = storage.getReference().child("images/" + uploadName);
                    UploadTask uploadTask = upload.putBytes(data);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(v.getContext(),"Failed to upload image",Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        }
                    });
                    documentReference.set(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(v.getContext(),"Post successfully!",Toast.LENGTH_SHORT).show();
                        }
                    });

                    finish();
                } else {
                    Toast.makeText(v.getContext(),"Need a picture to upload!",Toast.LENGTH_SHORT).show();
                }
            }
        });



    }



    //Check if content is empty
    private boolean validatePost(Post post) {
        if (postImg.getDrawable() != null) {
            return true;
        }
        return false;
    };
}