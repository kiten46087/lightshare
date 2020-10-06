package edu.neu.madcourse.share;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.neu.madcourse.share.Adapter.PostAdapter;
import edu.neu.madcourse.share.Model.Post;
import edu.neu.madcourse.share.Model.User;

public class UserPageActivity extends AppCompatActivity {
    private String userId;

    private ImageView userProfile;
    private TextView username;
    private TextView location;
    private TextView bio;
    private TextView following;
    private TextView followers;
    private RecyclerView recyclerView;

    private PostAdapter postAdapter;
    private List<Post> posts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        userProfile = findViewById(R.id.user_profile);
        username = findViewById(R.id.name);
        location = findViewById(R.id.location);
        bio = findViewById(R.id.bio);
        following = findViewById(R.id.following);
        followers = findViewById(R.id.followers);
        recyclerView = findViewById(R.id.recycler_view);

        // Get the USER Id.
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        //inflate profile
        userInfo();
        getFollowers();

        //inflate posts
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getBaseContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        RecyclerView.ItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(divider);

        posts = new ArrayList<>();
        postAdapter = new PostAdapter(getBaseContext(), posts);
        recyclerView.setAdapter(postAdapter);
        getMyPosts();
    }

    private void getMyPosts() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                posts.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post != null && post.getAuthorID() != null && post.getAuthorID().equals(userId)) {
                        posts.add(post);
                    }
                }
                Collections.reverse(posts);
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowers() {
        DatabaseReference followersReference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(userId).child("followers");
        DatabaseReference followingReference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(userId).child("following");

        followersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followers.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        followingReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                following.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void userInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                User user = snapshot.getValue(User.class);
                Glide.with(getBaseContext()).load(user.getImageurl()).into(userProfile);

                username.setText(user.getUsername());

                String locationStr = user.getLocation();
                if (locationStr == null || locationStr.isEmpty()) {
                    location.setVisibility(View.GONE);
                } else {
                    location.setText(locationStr);
                }

                String bioStr = user.getBio();
                if (bioStr == null || bioStr.isEmpty()) {
                    bio.setVisibility(View.GONE);
                } else {
                    bio.setText(bioStr);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}