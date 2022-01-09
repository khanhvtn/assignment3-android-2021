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
import androidx.core.content.FileProvider;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.assignment3.fragments.CaptureImageDialogFragment;
import com.example.assignment3.models.User;
import com.example.assignment3.utilities.Utility;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity
        implements CaptureImageDialogFragment.NoticeDialogListener {
    private static final String CAPTURE_IMAGE_DIALOG_TAG = "CAPTURE_IMAGE_DIALOG_TAG";
    private final String TAG = "EditProfileActivity";
    private AppCompatImageButton btnBack;
    private AppCompatButton btnChangeAvatar, btnUpdate;
    private CircleImageView userAvatar;
    private AppCompatEditText edtFullName, edtAddress, edtBio;
    private Boolean imageChange = false;
    private String newImageName = "";
    private Uri imageUri;
    private ActivityResultLauncher captureImageLauncher, pickImageFromPhoto;
    private CaptureImageDialogFragment captureImageDialogFragment;
    private AlertDialog loadingProgress;
    private User currentUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

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
                            newImageName =
                                    newImageName.isEmpty() ? UUID.randomUUID().toString() + ".jpg" :
                                            newImageName;
                            Glide.with(getApplicationContext()).load(imageUri).into(userAvatar);
                            imageChange = true;
                        }
                    }
                });
        pickImageFromPhoto = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        if (result != null) {
                            imageUri = result;
                            newImageName =
                                    newImageName.isEmpty() ? UUID.randomUUID().toString() + ".jpg" :
                                            newImageName;
                            Glide.with(getApplicationContext()).load(imageUri).into(userAvatar);
                            imageChange = true;
                        }
                    }
                });

        //declare fields
        btnBack = findViewById(R.id.btnBack);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        btnUpdate = findViewById(R.id.btnUpdate);
        userAvatar = findViewById(R.id.userAvatar);
        edtFullName = findViewById(R.id.edtFullName);
        edtAddress = findViewById(R.id.edtAddress);
        edtBio = findViewById(R.id.edtBio);

        //display user info
        Utility.firebaseFirestore.collection(getString(R.string.user_collection))
                .document(Utility.firebaseAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(
                        new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                User user = documentSnapshot.toObject(User.class);
                                currentUserInfo = user;
                                //display user avatar
                                if (user.getImageFileName() != null) {
                                    Utility.firebaseStorage.getReference()
                                            .child("images/" + user.getImageFileName())
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
                                        }
                                    });
                                }
                                newImageName = user.getImageFileName() == null ? "" :
                                        user.getImageFileName();
                                edtFullName.setText(user.getFullName());
                                edtAddress.setText(user.getAddress());
                                edtBio.setText(user.getBio() == null ? "" : user.getBio());

                            }
                        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, e.getMessage());
                Utility.ToastMessage(e.getMessage(), getApplicationContext());
            }
        });

        //set listener for button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingProgress.show();
                String fullName = edtFullName.getText().toString().trim();
                String address = edtAddress.getText().toString().trim();
                String bio = edtBio.getText().toString().trim();
                String validateResult =
                        validateUserInput(fullName, address);
                if (validateResult != null) {
                    Utility.ToastMessage(validateResult, getApplicationContext());
                    loadingProgress.dismiss();
                } else {
                    if (imageChange) {
                        UploadTask uploadTask =
                                Utility.firebaseStorage.getReference()
                                        .child("images/" + newImageName)
                                        .putFile(imageUri);
                        uploadTask.addOnSuccessListener(
                                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        currentUserInfo.setImageFileName(newImageName);
                                        currentUserInfo.setFullName(fullName);
                                        currentUserInfo.setAddress(address);
                                        currentUserInfo.setBio(bio.isEmpty() ? "" : bio);

                                        Utility.firebaseFirestore
                                                .collection(getString(R.string.user_collection))
                                                .document(Utility.firebaseAuth.getCurrentUser()
                                                        .getUid())
                                                .set(currentUserInfo, SetOptions
                                                        .merge()).addOnSuccessListener(
                                                new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        loadingProgress.dismiss();
                                                        finish();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                loadingProgress.dismiss();
                                                Utility.ToastMessage(
                                                        "Something went wrong. Please try again!!!",
                                                        getApplicationContext());
                                            }
                                        });
                                    }
                                });
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingProgress.dismiss();
                                Utility.ToastMessage(
                                        "Something went wrong with uploading image. Please try again!!!",
                                        getApplicationContext());
                            }
                        });
                    } else {
                        currentUserInfo.setImageFileName(newImageName);
                        currentUserInfo.setFullName(fullName);
                        currentUserInfo.setAddress(address);
                        currentUserInfo.setBio(bio.isEmpty() ? "" : bio);

                        Utility.firebaseFirestore.collection(getString(R.string.user_collection))
                                .document(Utility.firebaseAuth.getCurrentUser().getUid())
                                .set(currentUserInfo, SetOptions
                                        .merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        loadingProgress.dismiss();
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingProgress.dismiss();
                                Utility.ToastMessage(
                                        "Something went wrong. Please try again!!!",
                                        getApplicationContext());
                            }
                        });
                    }

                }

            }
        });
        btnChangeAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImageDialogFragment = new CaptureImageDialogFragment();
                captureImageDialogFragment
                        .show(getSupportFragmentManager(), CAPTURE_IMAGE_DIALOG_TAG);
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

    public String validateUserInput(String fullName,
                                    String address) {
        if (fullName.isEmpty()) {
            return "Full name can not be blanked";
        }
        if (address.isEmpty()) {
            return "Address can not be blanked";
        }

        return null;
    }
}