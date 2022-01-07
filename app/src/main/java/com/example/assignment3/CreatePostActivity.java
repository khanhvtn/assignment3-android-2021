package com.example.assignment3;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.assignment3.fragments.CaptureImageDialogFragment;
import com.example.assignment3.models.Post;
import com.example.assignment3.models.User;
import com.example.assignment3.utilities.Utility;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.UUID;

public class CreatePostActivity extends AppCompatActivity
        implements CaptureImageDialogFragment.NoticeDialogListener {
    private static final String CAPTURE_IMAGE_DIALOG_TAG = "CAPTURE_IMAGE_DIALOG_TAG";
    private final String TAG = "CreatePostActivity";
    private AppCompatImageButton btnBack;
    private AppCompatButton btnPublish, btnAddPhoto, btnRemovePhoto;
    private AppCompatEditText edtContent;
    private AppCompatImageView imageContent;
    private CardView imageContentCardView;
    private Boolean hasImageContent = false;
    private Uri imageUri;
    private ActivityResultLauncher captureImageLauncher, pickImageFromPhoto;
    private CaptureImageDialogFragment captureImageDialogFragment;
    private AlertDialog loadingProgress;
    private User currentUserInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        //generate progress dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.loading_progress).setCancelable(false);
        loadingProgress = builder.create();

        //create temporary image
        File newFile = new File(getApplicationContext().getFilesDir(), "default_image.jpg");
        imageUri = FileProvider
                .getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider",
                        newFile);
        //set activity result launcher
        captureImageLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean result) {
                        Log.i(TAG, "Capture Image");
                        if (result) {
                            Log.i(TAG, "Capture Image Successfully");
                            Glide.with(getApplicationContext()).load(imageUri).into(imageContent);
                            hasImageContent = true;
                            imageContentCardView.setVisibility(View.VISIBLE);
                            btnAddPhoto.setVisibility(View.GONE);
                            btnRemovePhoto.setVisibility(View.VISIBLE);
                        }
                    }
                });
        pickImageFromPhoto = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        if (result != null) {
                            imageUri = result;
                            Glide.with(getApplicationContext()).load(imageUri).into(imageContent);
                            hasImageContent = true;
                            imageContentCardView.setVisibility(View.VISIBLE);
                            btnAddPhoto.setVisibility(View.GONE);
                            btnRemovePhoto.setVisibility(View.VISIBLE);
                        }
                    }
                });
        //declare fields
        btnBack = findViewById(R.id.btnBack);
        btnPublish = findViewById(R.id.btnPublish);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        btnRemovePhoto = findViewById(R.id.btnRemovePhoto);
        edtContent = findViewById(R.id.edtContent);
        imageContent = findViewById(R.id.imageContent);
        imageContentCardView = findViewById(R.id.imageContentCardView);

        //set btn listener
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImageDialogFragment = new CaptureImageDialogFragment();
                captureImageDialogFragment
                        .show(getSupportFragmentManager(), CAPTURE_IMAGE_DIALOG_TAG);
            }
        });
        btnRemovePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hasImageContent = false;
                imageContentCardView.setVisibility(View.GONE);
                btnAddPhoto.setVisibility(View.VISIBLE);
                btnRemovePhoto.setVisibility(View.GONE);
            }
        });

        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingProgress.show();
                String textContent = edtContent.getText().toString();
                if (textContent.isEmpty() && !hasImageContent) {
                    Utility.ToastMessage("Please input content before publish",
                            getApplicationContext());
                    loadingProgress.dismiss();
                } else {
                    Post newPost = new Post(currentUserInfo.getUserId(),
                            textContent.isEmpty() ? null : textContent, null);
                    if (hasImageContent) {
                        String imageFileName = UUID.randomUUID().toString() + ".jpg";
                        Utility.firebaseStorage.getReference().child("images/" + imageFileName)
                                .putFile(imageUri).addOnSuccessListener(
                                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        newPost.setImageContentFileName(imageFileName);
                                        Utility.firebaseFirestore
                                                .collection(getString(R.string.post_collection))
                                                .add(newPost).addOnSuccessListener(
                                                new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(
                                                            DocumentReference documentReference) {
                                                        newPost.setPostId(
                                                                documentReference.getId());
                                                        documentReference
                                                                .set(newPost, SetOptions.merge());
                                                        Utility.ToastMessage(
                                                                "Create Post Successfully",
                                                                getApplicationContext());
                                                        loadingProgress.dismiss();
                                                        finish();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.i(TAG, e.getMessage());
                                                Utility.ToastMessage(
                                                        "Something went wrong. Please try again",
                                                        getApplicationContext());
                                                loadingProgress.dismiss();
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i(TAG, e.getMessage());
                                Utility.ToastMessage(
                                        "Something went wrong. Please try again",
                                        getApplicationContext());
                                loadingProgress.dismiss();
                            }
                        });
                    } else {
                        Utility.firebaseFirestore
                                .collection(getString(R.string.post_collection))
                                .add(newPost).addOnSuccessListener(
                                new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(
                                            DocumentReference documentReference) {
                                        newPost.setPostId(
                                                documentReference.getId());
                                        documentReference
                                                .set(newPost, SetOptions.merge());
                                        Utility.ToastMessage(
                                                "Create Post Successfully",
                                                getApplicationContext());
                                        loadingProgress.dismiss();
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i(TAG, e.getMessage());
                                Utility.ToastMessage(
                                        "Something went wrong. Please try again",
                                        getApplicationContext());
                                loadingProgress.dismiss();
                            }
                        });
                    }

                }
            }
        });

        //get current user info
        Utility.firebaseFirestore.collection(getString(R.string.user_collection))
                .document(Utility.firebaseAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(
                        new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                User user = documentSnapshot.toObject(User.class);
                                currentUserInfo = user;
                            }
                        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, e.getMessage());
            }
        });
    }

    @Override
    public void onPickingOption(int optionIndex) {
        switch (optionIndex) {
            case 0:
                captureImageLauncher.launch(this.imageUri);
                break;
            case 1:
                pickImageFromPhoto.launch("image/*");
                break;
        }
    }
}