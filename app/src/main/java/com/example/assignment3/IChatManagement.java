package com.example.assignment3;

import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;

public interface IChatManagement {
    public void switchFragmentInMainActivity(Fragment fragment);

    public void setUpForPickImageDialog(ActivityResultLauncher captureImageLauncher,
                                        ActivityResultLauncher pickImageFromPhoto, Uri imageUri);
}
