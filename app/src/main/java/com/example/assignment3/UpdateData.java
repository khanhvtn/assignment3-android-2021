package com.example.assignment3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.assignment3.models.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

public class UpdateData extends AppCompatActivity {

    ImageView image;
    TextView photoChange;
    Button updateBtn, homeBtn;
    EditText existingName, newName, newAddress, newEmail, newImgURL;
    FirebaseFirestore db;
    FirebaseAuth fAuth;
    String userId;
    private Uri ImageUri;
    private StorageReference storageReference;
    private StorageTask uploadTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_data);
        db = FirebaseFirestore.getInstance();
        image =findViewById(R.id.image_profile);
        photoChange = findViewById(R.id.textView_change);
        updateBtn = findViewById(R.id.updatebtn);
        homeBtn = findViewById(R.id.homebtn);
        newName = findViewById(R.id.newName);
        newAddress = findViewById(R.id.newAddress);
        newEmail = findViewById(R.id.newEmail);
        db = FirebaseFirestore.getInstance();

        fAuth = FirebaseAuth.getInstance();
        userId = fAuth.getCurrentUser().getUid();

        DocumentReference documentReference = db.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                newName.setText(value.getString("fullName"));
                newAddress.setText(value.getString("address"));
                newEmail.setText(value.getString("email"));
            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateData.this, MainActivity.class);
                startActivity(intent);
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newname = newName.getText().toString();
                String newaddress = newAddress.getText().toString();
                String newemail = newEmail.getText().toString();
                UpdateData(newname, newaddress, newemail);
            }
        });

        // when user clicks on "change profile photo"
        photoChange.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .setCropShape(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? CropImageView.CropShape.RECTANGLE : CropImageView.CropShape.OVAL)
                        .start(UpdateData.this);
            }
        });

        // when user clicks on the profile image photo
        image.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .setCropShape(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? CropImageView.CropShape.RECTANGLE : CropImageView.CropShape.OVAL)
                        .start(UpdateData.this);
            }
        });
    }

    private void UpdateData(String newname, String newAddress, String newEmail) {
        Map<String,Object> userDetail = new HashMap<>();
        userDetail.put("fullName", newname);
        userDetail.put("address", newAddress);
        userDetail.put("email", newEmail);

        db.collection("users")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if(task.isSuccessful() && !task.getResult().isEmpty()){
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    String currentUserId = user.getUid();
                    db.collection("users")
                            .document(currentUserId)
                            .update(userDetail)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                    Toast.makeText(UpdateData.this, "Successfully Updated", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UpdateData.this, "Some Error Occured", Toast.LENGTH_SHORT).show();

                        }
                    });
                }else{
                    Toast.makeText(UpdateData.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private String getFileExtension(Uri uri)
    {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage()
    {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading");
        progressDialog.show();


        if (ImageUri != null)
        {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(ImageUri));
            uploadTask = fileReference.putFile(ImageUri);
            uploadTask.continueWithTask(new Continuation()
            {
                @Override
                public Object then(@NonNull Task task) throws Exception
                {
                    if (! task.isSuccessful())
                    {
                        throw task.getException();
                    }

                    return fileReference.getDownloadUrl();


                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    if(task.isSuccessful()){
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String currentUserId = user.getUid();
                        Uri downloadUri = task.getResult();
                        String myUrl =downloadUri.toString();
                        Map<String, Object> imageUri =new HashMap<>();
                        imageUri.put("imageFileName", myUrl);
                        db.collection("users")
                                .document(currentUserId)
                                .update(imageUri)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                        Toast.makeText(UpdateData.this, "Successfully Updated", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(UpdateData.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });
                    }else{
                        Toast.makeText(UpdateData.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else
        {
            Toast.makeText(this, "No images selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            ImageUri = result.getUri();
            uploadImage();
        }
        else
        {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }
}
