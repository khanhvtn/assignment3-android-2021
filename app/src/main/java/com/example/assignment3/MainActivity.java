package com.example.assignment3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.assignment3.fragments.CurrentUserProfile;
import com.example.assignment3.fragments.Home;
import com.example.assignment3.utilities.Utility;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements IMainManagement {
    private BottomNavigationView main_bottomNavigationBar;
    private View itemProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, Authentication.class));
        }

        //declare fields
        main_bottomNavigationBar = findViewById(R.id.main_bottomNavigationBar);


        //add map fragment to activity
        Fragment homeFragment = new Home();
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.main_fragmentContainer, homeFragment,
                        homeFragment.getClass().toString())
                .commit();


        //declare fields
        AppCompatButton btnLogout = findViewById(R.id.main_btnLogout);
        AppCompatButton btnChatRoom = findViewById(R.id.main_btnChatRoom);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(view.getContext(), Authentication.class));
            }
        });
        btnChatRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), ChatActivity.class));
            }
        });
        itemProfile = main_bottomNavigationBar.findViewById(R.id.item_profile);

        main_bottomNavigationBar.setOnItemSelectedListener(
                new NavigationBarView.OnItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.item_home:
                                switchFragmentInMainActivity(new Home());
                                break;
                            case R.id.item_explore:
//                                switchFragmentInMainActivity(new ListCampaign());
                                Utility.ToastMessage("Go To Explore Fragment", getBaseContext());
                                break;
                            case R.id.item_notifications:
//                                switchFragmentInMainActivity(new GenerateReport());
                                Utility.ToastMessage("Go To Notification Fragment",
                                        getBaseContext());
                                break;
                            case R.id.item_profile:
                                switchFragmentInMainActivity(new CurrentUserProfile());
                                break;
                        }
                        return true;
                    }
                });

    }

    @Override
    public void switchFragmentInMainActivity(Fragment fragment) {
        Fragment currentFragment =
                getSupportFragmentManager().findFragmentById(R.id.main_fragmentContainer);
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
                    .replace(R.id.main_fragmentContainer, fragment, fragment.getClass().toString())
                    .commit();
        }
    }

    @Override
    public void switchToProfile() {
        itemProfile.performClick();
    }
}