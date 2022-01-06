package com.cosc2657.assignment3.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.cosc2657.assignment3.Adapter.PostAdapter;
import com.cosc2657.assignment3.Adapter.StoryAdapter;
import com.cosc2657.assignment3.Model.Post;
import com.cosc2657.assignment3.Model.Story;
import com.cosc2657.assignment3.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment
{
    // for regular posts
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postLists;

    // for stories
    private RecyclerView recyclerViewStory;
    private StoryAdapter storyAdapter;
    private List<Story> storyList;


    private List<String> followingList;
    ProgressBar progressBar;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        // For Regular Posts
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        postLists = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postLists);
        recyclerView.setAdapter(postAdapter);


        // For Story Feature
        recyclerViewStory = view.findViewById(R.id.recycler_view_story);
        recyclerViewStory.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewStory.setLayoutManager(linearLayoutManager1);
        storyList = new ArrayList<>();
        storyAdapter = new StoryAdapter(getContext(), storyList);
        recyclerViewStory.setAdapter(storyAdapter);




        progressBar = view.findViewById(R.id.progress_circular);

        checkFollowing();

        return view;
    }





    private void checkFollowing()
    {
        followingList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("Following");


        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                followingList.clear();

                for (DataSnapshot snapshot: dataSnapshot.getChildren())
                {
                    followingList.add(snapshot.getKey());
                }

                readPosts();
                readStory();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });
    }





    private void readPosts()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                postLists.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Post post = snapshot.getValue(Post.class);

                    for (String id: followingList)
                    {
                        if (post.getPublisher().equals(id))
                        {
                            postLists.add(post);
                        }
                    }
                }

                postAdapter.notifyDataSetChanged();

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });
    }





    private void readStory()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story");

        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                long timeCurrent = System.currentTimeMillis();

                storyList.clear();
                storyList.add( new Story("", "", 0, 0, FirebaseAuth.getInstance().getCurrentUser().getUid()));


                for (String id: followingList)
                {
                    int storyCount = 0;

                    Story story = null;


                    for (DataSnapshot snapshot: dataSnapshot.child(id).getChildren())
                    {
                        story = snapshot.getValue(Story.class);

                        if (timeCurrent > story.getTimeStart() && timeCurrent < story.getTimeEnd())
                        {
                            storyCount++;
                        }
                    }

                    if (storyCount > 0)
                    {
                        storyList.add(story);
                    }
                }



                storyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });
    }
}
