package com.example.assignment3;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment3.Explore.SearchUserAdapter;

public interface IExploreManagement {
    public void switchFragmentInMainActivity(Fragment fragment);
    public void setSearchUserAdapter(SearchUserAdapter searchUserAdapter);
    public void setRvUser(RecyclerView recyclerView);
}
