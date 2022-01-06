package com.example.assignment3;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.net.Uri;
import android.os.Bundle;

import com.example.assignment3.fragments.CaptureImageDialogFragment;
import com.example.assignment3.fragments.ChatRoom;

public class ChatActivity extends AppCompatActivity implements IChatManagement,
        CaptureImageDialogFragment.NoticeDialogListener {

    private ActivityResultLauncher captureImageLauncher, pickImageFromPhoto;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        //add map fragment to activity
        Fragment chatRoomFragment = new ChatRoom();
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.chat_fragment_container, chatRoomFragment,
                        chatRoomFragment.getClass().toString())
                .commit();
    }

    @Override
    public void switchFragmentInMainActivity(Fragment fragment) {
        Fragment currentFragment =
                getSupportFragmentManager().findFragmentById(R.id.chat_fragment_container);
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
                    .replace(R.id.chat_fragment_container, fragment, fragment.getClass().toString())
                    .commit();
        }
    }

    @Override
    public void setUpForPickImageDialog(ActivityResultLauncher captureImageLauncher,
                                        ActivityResultLauncher pickImageFromPhoto, Uri imageUri) {
        this.captureImageLauncher = captureImageLauncher;
        this.pickImageFromPhoto = pickImageFromPhoto;
        this.imageUri = imageUri;
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