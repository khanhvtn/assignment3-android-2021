package com.example.assignment3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;

import com.example.assignment3.fragments.Login;
import com.example.assignment3.utilities.Utility;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Authentication extends AppCompatActivity implements IAuthenticationManagement {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        Log.i("Authentication", Utility.genUID("KxRYaqUyYvQtotwti1cTNf7Qkiz2", "P2Es3qrx8ufzhWSGT8tXCX9rewz1"));

        //add map fragment to activity
        Fragment loginFragment = new Login();
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.auth_fragment_container, loginFragment, loginFragment.getClass().toString())
                .commit();

    }


    @Override
    public void switchFragmentInMainActivity(Fragment fragment) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.auth_fragment_container);
        if (!fragment.getClass().toString().equals(currentFragment.getTag())) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in,  // enter
                            R.anim.fade_out,  // exit
                            R.anim.fade_in,   // popEnter
                            R.anim.slide_out  // popExit
                    )
                    .setReorderingAllowed(true)
                    .replace(R.id.auth_fragment_container, fragment, fragment.getClass().toString())
                    .commit();
        }
    }

    @Override
    public void goToHomeScreen() {

    }
}