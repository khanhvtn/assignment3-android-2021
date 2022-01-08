package com.example.assignment3;

import androidx.fragment.app.Fragment;

public interface IMainManagement {
    public void switchFragmentInMainActivity(Fragment fragment);

    public void switchToProfile();

    public void removeNotificationListener();
}
