package com.cosc2657.assignment3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity
{
    EditText userName;
    EditText fullName;
    EditText email;
    EditText password;
    Button register;
    TextView textLogin;

    FirebaseAuth auth;
    DatabaseReference reference;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        // connect variables with UI nodes
        userName = findViewById(R.id.username);
        fullName = findViewById(R.id.fullName);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        register = findViewById(R.id.register);
        textLogin = findViewById(R.id.textLogin);


        auth = FirebaseAuth.getInstance();


        textLogin.setOnClickListener(new View.OnClickListener()     // When user clicks on the "Already have an Account? login" TextView
        {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });



        register.setOnClickListener(new View.OnClickListener()      // When user clicks on the "register" button after filling out the info.
        {
            @Override
            public void onClick(View v)
            {
                progressDialog = new ProgressDialog(RegisterActivity.this);
                progressDialog.setMessage("Please Wait...");
                progressDialog.show();

                String usernameAsString = userName.getText().toString();
                String fullNameAsString = fullName.getText().toString();
                String emailAsString = email.getText().toString();
                String passwordAsString = password.getText().toString();


                if (TextUtils.isEmpty(usernameAsString) || TextUtils.isEmpty(fullNameAsString) || TextUtils.isEmpty(emailAsString) || TextUtils.isEmpty(passwordAsString))
                {
                    Toast.makeText(RegisterActivity.this, "All fields are required!", Toast.LENGTH_SHORT).show();
                }
                else if (passwordAsString.length() < 6)
                {
                    Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    register(usernameAsString, fullNameAsString, emailAsString, passwordAsString);
                }
            }
        });
    }





    private void register(final String username, final String fullName, String email, String password)
    {

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            // registration of user is successful
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            String userId = firebaseUser.getUid();
                            reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("id", userId);
                            hashMap.put("username", username.toLowerCase());
                            hashMap.put("fullName", fullName);
                            hashMap.put("bio", "");
                            hashMap.put("imageURL", "https://firebasestorage.googleapis.com/v0/b/instagram-clone-ea6ff.appspot.com/o/placeholder.png?alt=media&token=68bc014d-29e3-49fa-96db-eb9c642cf034");


                            // add the newly created user account to Firebase database
                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        progressDialog.dismiss();

                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);



                                        //   operator ( | ) is the "bitwise OR"
                                        // the output of bitwise OR on two bits is 1 if either bit is 1, or 0 if both bits are 0.



                                        // Intent
                                        //  - messaging object used to request an action from another app component
                                        //  - facilitates communication between different components
                                        //     1) Explicit Intent - 2 activities inside the same application      setClass(Context, Class)     which provides the exact class to be run.
                                        //     2) Implicit Intent - 2 activities of different application


                                        // Using Intent Flags
                                        //      - When starting an activity, you can modify the default association of an activity to its task by including flags in the intent that you deliver to startActivity()

                                        // Task
                                        //      - collection of activities that users interact with when performing a certain job.
                                        //      - The activities are arranged in a stack (back stack) in the order in which each activity is opened.
                                        //      - if user presses the back button, the activity that was just added us now finished and popped off the stack.
                                    }
                                }
                            });
                        }
                        else
                        {
                            progressDialog.dismiss();

                            Toast.makeText(RegisterActivity.this, "You can't register with this email or password", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }
}
