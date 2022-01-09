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
import com.example.assignment3.models.Follower;
import com.example.assignment3.models.Post;
import com.example.assignment3.models.User;
import com.example.assignment3.utilities.Utility;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class EditPostActivity extends AppCompatActivity
        implements CaptureImageDialogFragment.NoticeDialogListener {
    private static final String CAPTURE_IMAGE_DIALOG_TAG = "CAPTURE_IMAGE_DIALOG_TAG";
    private final String TAG = "EditPostActivity";
    private AppCompatImageButton btnBack;
    private AppCompatButton btnDone, btnAddPhoto, btnRemovePhoto;
    private AppCompatEditText edtContent;
    private AppCompatImageView imageContent;
    private CardView imageContentCardView;
    private Boolean hasImageContent = false;
    private Boolean hasImageChange = false;
    private Uri imageUri;
    private ActivityResultLauncher captureImageLauncher, pickImageFromPhoto;
    private CaptureImageDialogFragment captureImageDialogFragment;
    private AlertDialog loadingProgress;
    private User currentUserInfo;
    private Post currentPostInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        //get post id
        String postId = getIntent().getStringExtra("postId");

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
                            hasImageChange = true;
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
                            hasImageChange = true;
                            imageContentCardView.setVisibility(View.VISIBLE);
                            btnAddPhoto.setVisibility(View.GONE);
                            btnRemovePhoto.setVisibility(View.VISIBLE);
                        }
                    }
                });
        //declare fields
        btnBack = findViewById(R.id.btnBack);
        btnDone = findViewById(R.id.btnDone);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        btnRemovePhoto = findViewById(R.id.btnRemovePhoto);
        edtContent = findViewById(R.id.edtContent);
        imageContent = findViewById(R.id.imageContent);
        imageContentCardView = findViewById(R.id.imageContentCardView);

        //set current post info
        Utility.firebaseFirestore.collection(getString(R.string.post_collection)).document(postId)
                .get().addOnSuccessListener(
                new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Post post = documentSnapshot.toObject(Post.class);
                        currentPostInfo = post;
                        if (post.getTextContent() != null) {
                            edtContent.setText(post.getTextContent());
                        }
                        if (post.getImageContentFileName() != null) {
                            Utility.firebaseStorage.getReference()
                                    .child("images/" + post.getImageContentFileName())
                                    .getDownloadUrl().addOnSuccessListener(
                                    new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Glide.with(getApplicationContext()).load(uri)
                                                    .into(imageContent);
                                            hasImageContent = true;
                                            imageContentCardView.setVisibility(View.VISIBLE);
                                            btnAddPhoto.setVisibility(View.GONE);
                                            btnRemovePhoto.setVisibility(View.VISIBLE);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, e.getMessage());
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, e.getMessage());
            }
        });

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

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingProgress.show();
                String textContent = edtContent.getText().toString();
                if (textContent.isEmpty() && !hasImageContent) {
                    Utility.ToastMessage("Please input content before edit your post",
                            getApplicationContext());
                    loadingProgress.dismiss();
                } else {
                    currentPostInfo.setTextContent(textContent.isEmpty() ? null : textContent);
                    if (hasImageContent) {
                        if (hasImageChange) {
                            String newImageName =
                                    currentPostInfo.getImageContentFileName() == null ?
                                            UUID.randomUUID().toString() :
                                            currentPostInfo.getImageContentFileName();
                            Utility.firebaseStorage.getReference()
                                    .child("images/" + newImageName)
                                    .putFile(imageUri).addOnSuccessListener(
                                    new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(
                                                UploadTask.TaskSnapshot taskSnapshot) {
                                            currentPostInfo.setImageContentFileName(newImageName);
                                            Utility.firebaseFirestore
                                                    .collection(getString(R.string.post_collection))
                                                    .document(postId)
                                                    .set(currentPostInfo, SetOptions.merge())
                                                    .addOnSuccessListener(
                                                            new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    Utility.ToastMessage(
                                                                            "Update Post Successfully",
                                                                            getApplicationContext());
                                                                    loadingProgress.dismiss();
                                                                    finish();
                                                                }
                                                            })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(
                                                                @NonNull Exception e) {
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
                                    .document(postId).set(currentPostInfo, SetOptions.merge())
                                    .addOnSuccessListener(
                                            new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Utility.ToastMessage(
                                                            "Update Post Successfully",
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
                    } else {
                        if (currentPostInfo.getImageContentFileName() == null) {
                            Utility.firebaseFirestore
                                    .collection(getString(R.string.post_collection))
                                    .document(postId)
                                    .set(currentPostInfo, SetOptions.merge())
                                    .addOnSuccessListener(
                                            new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Utility.ToastMessage(
                                                            "Update Post Successfully",
                                                            getApplicationContext());
                                                    loadingProgress.dismiss();
                                                    finish();
                                                }
                                            })
                                    .addOnFailureListener(new OnFailureListener() {
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
                            Utility.firebaseStorage.getReference()
                                    .child("images/" + currentPostInfo.getImageContentFileName())
                                    .delete().addOnSuccessListener(
                                    new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            currentPostInfo.setImageContentFileName(null);
                                            Utility.firebaseFirestore
                                                    .collection(getString(R.string.post_collection))
                                                    .document(postId)
                                                    .set(currentPostInfo, SetOptions.merge())
                                                    .addOnSuccessListener(
                                                            new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    Utility.ToastMessage(
                                                                            "Update Post Successfully",
                                                                            getApplicationContext());
                                                                    loadingProgress.dismiss();
                                                                    finish();
                                                                }
                                                            })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(
                                                                @NonNull Exception e) {
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
                        }


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