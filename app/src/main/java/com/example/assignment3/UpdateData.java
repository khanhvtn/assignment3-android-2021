package com.example.assignment3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.assignment3.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class UpdateData extends AppCompatActivity {

    Button updateBtn;
    EditText existingName, newName, newAddress, newEmail, newImgURL;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_data);
        db = FirebaseFirestore.getInstance();
        updateBtn = findViewById(R.id.updatebtn);
        existingName = findViewById(R.id.existingFullName);
        newName = findViewById(R.id.newName);
        newAddress = findViewById(R.id.newAddress);
        newEmail = findViewById(R.id.newEmail);


        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = existingName.getText().toString();
                String newname = newName.getText().toString();
                String newaddress = newAddress.getText().toString();
                String newemail = newEmail.getText().toString();
                existingName.setText("");
                newName.setText("");
                newAddress.setText("");
                newEmail.setText("");
                UpdateData(name, newname, newaddress, newemail);
            }
        });
    }

    private void UpdateData(String name, String newname, String newAddress, String newEmail) {
        Map<String,Object> userDetail = new HashMap<>();
        userDetail.put("fullName", newname);
        userDetail.put("address", newAddress);
        userDetail.put("email", newEmail);

        db.collection("users")
                .whereEqualTo("fullName", name)
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
}