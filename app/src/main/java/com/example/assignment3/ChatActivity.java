package com.example.assignment3;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.example.assignment3.fragments.CaptureImageDialogFragment;
import com.example.assignment3.fragments.Chat;
import com.example.assignment3.fragments.ChatRoom;
import com.example.assignment3.models.User;
import com.example.assignment3.utilities.Utility;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;

public class ChatActivity extends AppCompatActivity implements IChatManagement,
        CaptureImageDialogFragment.NoticeDialogListener {

    private final String TAG = "ChatActivity";
    private ActivityResultLauncher captureImageLauncher, pickImageFromPhoto;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        //get intent
        String targetUserIdForChat = getIntent().getStringExtra("userId");

        if (targetUserIdForChat == null) {
            //add chatroom fragment to activity
            Fragment chatRoomFragment = new ChatRoom();
            getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.chat_fragment_container, chatRoomFragment,
                            chatRoomFragment.getClass().toString())
                    .commit();
        } else {
            Utility.firebaseFirestore.collection(getString(R.string.user_collection))
                    .document(targetUserIdForChat).get().addOnSuccessListener(
                    new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            User user = documentSnapshot.toObject(User.class);
                            Bundle result = new Bundle();
                            String[] authorInfo =
                                    {targetUserIdForChat, user.getFullName(),
                                            user.getImageFileName() !=
                                                    null ? user
                                                    .getImageFileName() : ""};

                            result.putStringArray("author_info", authorInfo);
                            result.putBoolean("startFromProfile", true);
                            getSupportFragmentManager()
                                    .setFragmentResult("requestKey", result);
                            //add chat fragment to activity
                            Fragment chatFragment = new Chat();
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .setReorderingAllowed(true)
                                    .replace(R.id.chat_fragment_container, chatFragment,
                                            chatFragment.getClass().toString())
                                    .commit();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Utility.ToastMessage(e.getMessage(), getBaseContext());
                    Log.i(TAG, e.getMessage());
                }
            });

        }


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