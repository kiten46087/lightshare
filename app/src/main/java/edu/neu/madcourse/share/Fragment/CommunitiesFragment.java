package edu.neu.madcourse.share.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import edu.neu.madcourse.share.Adapter.SingleCommunityAdapter;
import edu.neu.madcourse.share.CreateCommunityActivity;
import edu.neu.madcourse.share.Model.Community;
import edu.neu.madcourse.share.R;
import edu.neu.madcourse.share.SearchCommunityActivity;

public class CommunitiesFragment extends Fragment {

    private RecyclerView recyclerView;
    private SingleCommunityAdapter communityAdapter;
    private List<Community> communities;
    private ImageView create;
    private ImageView search;

    RecyclerView.LayoutManager layoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_communities, container, false);
        // Inflate the layout for this fragment

        recyclerView = view.findViewById(R.id.recycler_view);
        layoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);


        communities = new ArrayList<>();
        // Add values to the communities ArrayList.
        getSubscribedCommunities();
        communityAdapter = new SingleCommunityAdapter(getContext(), communities);
        recyclerView.setAdapter(communityAdapter);

        //set search on click
        search = view.findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchCommunityActivity.class);
                startActivity(intent);
            }
        });

        //set create button
        create = view.findViewById(R.id.create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), CreateCommunityActivity.class));
            }
        });

        return view;
    }

    private void getAllCommunities() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Communities");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                communities.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Community community = dataSnapshot.getValue(Community.class);
                    communities.add(community);
                }

                communityAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getSubscribedCommunities() {
        final List<String> myCommunities = new ArrayList<>();
        final String curUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Subscribe")
                .child(curUserID);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myCommunities.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    myCommunities.add(dataSnapshot.getKey());
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
                communities.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Community community = dataSnapshot.getValue(Community.class);
                    for (String communityId : myCommunities) {
                        assert community != null;
                        if (communityId.equals(community.getCommunityId())) {
                            communities.add(community);
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