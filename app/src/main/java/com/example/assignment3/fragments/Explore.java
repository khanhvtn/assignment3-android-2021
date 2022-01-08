package com.example.assignment3.fragments;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.assignment3.Explore.SearchUserAdapter;
import com.example.assignment3.IExploreManagement;
import com.example.assignment3.IMainManagement;
import com.example.assignment3.R;
import com.example.assignment3.utilities.Utility;

public class Explore extends Fragment
        implements IExploreManagement, SearchView.OnQueryTextListener {
    private SearchView searchView;
    private AppCompatButton btnCancel;
    private SearchUserAdapter searchUserAdapter;
    private RecyclerView rv_user;
    private IMainManagement mainManagement;

    public Explore() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainManagement = (IMainManagement) getActivity();
        //start first fragment
        ExplorePost explorePostFragment = new ExplorePost();
        getChildFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out  // popExit
                )
                .setReorderingAllowed(true)
                .replace(R.id.fragmentContainer, explorePostFragment,
                        explorePostFragment.getClass().toString())
                .commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_explore, container, false);
        //declare fields
        searchView = v.findViewById(R.id.searchView);
        btnCancel = v.findViewById(R.id.btnCancel);
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    btnCancel.setVisibility(View.VISIBLE);
                    switchFragmentInMainActivity(new ExploreSearch());
                } else {
                    btnCancel.setVisibility(View.GONE);
                    switchFragmentInMainActivity(new ExplorePost());
                }

            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.clearFocus();
            }
        });
        searchView.setOnQueryTextListener(this);
        return v;
    }

    @Override
    public void switchFragmentInMainActivity(Fragment fragment) {
        Fragment currentFragment =
                getChildFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (!fragment.getClass().toString().equals(currentFragment.getTag())) {
            getChildFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in,  // enter
                            R.anim.fade_out,  // exit
                            R.anim.fade_in,   // popEnter
                            R.anim.slide_out  // popExit
                    )
                    .setReorderingAllowed(true)
                    .replace(R.id.fragmentContainer, fragment, fragment.getClass().toString())
                    .commit();
        }
    }

    @Override
    public void setSearchUserAdapter(SearchUserAdapter searchUserAdapter) {
        this.searchUserAdapter = searchUserAdapter;
    }

    @Override
    public void setRvUser(RecyclerView recyclerView) {
        this.rv_user = recyclerView;
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        searchView.clearFocus();
        searchUserAdapter.filter("");
        return false;
    }

    @Override
    public boolean onQueryTextChange(String searchText) {
        if (!searchText.isEmpty()) {
            rv_user.setVisibility(View.VISIBLE);
            searchUserAdapter.filter(searchText);
        } else {
            rv_user.setVisibility(View.GONE);
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        mainManagement.switchFragmentInMainActivity(new Explore());
    }
}