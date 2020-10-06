package edu.neu.madcourse.share;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import edu.neu.madcourse.share.Adapter.CommunityAdapter;
import edu.neu.madcourse.share.Model.Community;

public class MyCommunityActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CommunityAdapter communityAdapter;
    private List<Community> communityList;
    final String curUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_community);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Created Communities");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getBaseContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        RecyclerView.ItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(divider);

        communityList = new ArrayList<>();
        communityAdapter = new CommunityAdapter(getBaseContext(), communityList);
        recyclerView.setAdapter(communityAdapter);

        getMyCommunities();
    }

    private void getMyCommunities() {
        final List<String> myCommunities = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Communities");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Community community = dataSnapshot.getValue(Community.class);
                    if (community.getCreator().equals(curUserID)) {
                        myCommunities.add(dataSnapshot.getKey());
                    }
                }

                addCommunities(myCommunities);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addCommunities(final List<String> myCommunities) {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Communities");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                communityList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Community community = dataSnapshot.getValue(Community.class);
                    for (String communityId : myCommunities) {
                        if (communityId.equals(community.getCommunityId())) {
                            communityList.add(community);
                        }
                    }
                }
                communityAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}