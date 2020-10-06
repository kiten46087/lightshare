package edu.neu.madcourse.share.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import edu.neu.madcourse.share.EditProfileActivity;
import edu.neu.madcourse.share.FollowersActivity;
import edu.neu.madcourse.share.Model.User;
import edu.neu.madcourse.share.MyCommunityActivity;
import edu.neu.madcourse.share.MyFavoritesActivity;
import edu.neu.madcourse.share.MyPostsActivity;
import edu.neu.madcourse.share.R;
import edu.neu.madcourse.share.SettingsActivity;

public class ProfileFragment extends Fragment {
    ImageView edit;
    ImageView imageProfile;
    TextView followers, following, fullname, bio, username, location;
    Button editProfile;
    LinearLayout selfLayout;

    FirebaseUser firebaseUser;
    String profileid;
    Boolean isSelf;

    private LinearLayout settingsLayout;
    private LinearLayout myPostsLayout;
    private LinearLayout myFavoritesLayout;
    private LinearLayout createdCommunitiesLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profileid = prefs.getString("profileid", "none");
        isSelf = prefs.getBoolean("isself", false);


        imageProfile = view.findViewById(R.id.image_profile);
        followers = view.findViewById(R.id.followers);
        following = view.findViewById(R.id.followering);
        fullname = view.findViewById(R.id.fullname);
        username = view.findViewById(R.id.username);
        bio = view.findViewById(R.id.bio);
//        edit_profile = view.findViewById(R.id.edit_profile);
        location = view.findViewById(R.id.location);
        selfLayout = view.findViewById(R.id.self_layout);

//        if (!isSelf) {
//            selfLayout.setVisibility(View.GONE);
////            following.setClickable(false);
////            followers.setClickable(false);
//        } else {
//            following.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(getContext(), FollowersActivity.class);
//                    intent.putExtra("id", profileid);
//                    intent.putExtra("title", "following");
//                    startActivity(intent);
//                }
//            });
//
//            followers.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(getContext(), FollowersActivity.class);
//                    intent.putExtra("id", profileid);
//                    intent.putExtra("title", "followers");
//                    startActivity(intent);
//                }
//            });
//        }

        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileid);
                intent.putExtra("title", "following");
                startActivity(intent);
            }
        });

        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileid);
                intent.putExtra("title", "followers");
                startActivity(intent);
            }
        });


        // Set all the info on these pages.
        userInfo();
        getFollowers();


        //set my posts on click listener
        myPostsLayout = view.findViewById(R.id.my_posts);
        myPostsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MyPostsActivity.class);
                startActivity(intent);
            }
        });

        //set my posts on click listener
        myFavoritesLayout = (LinearLayout) view.findViewById(R.id.my_favorites);
        myFavoritesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MyFavoritesActivity.class);
                startActivity(intent);
            }
        });

        //set up my settings
        settingsLayout = (LinearLayout) view.findViewById(R.id.my_settings);
        settingsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        createdCommunitiesLayout = (LinearLayout) view.findViewById(R.id.my_forum);
        createdCommunitiesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MyCommunityActivity.class);
                startActivity(intent);
            }
        });

        //set add person
        edit = view.findViewById(R.id.edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), EditProfileActivity.class));
            }
        });

        return view;
    }

    private void addNotifications() {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Notifications").child(profileid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userId", firebaseUser.getUid());
        hashMap.put("text", "started following you");
        hashMap.put("postId", "");
        hashMap.put("isPost", false);

        reference.push().setValue(hashMap);
    }

    private void userInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(profileid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (getContext() == null) {
                    return;
                }

                User user = snapshot.getValue(User.class);

                Glide.with(getContext()).load(user.getImageurl()).into(imageProfile);
                username.setText(user.getUsername());
                fullname.setText(user.getFullname());
                bio.setText(user.getBio());
                location.setText(user.getLocation());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void checkFollow() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(profileid).exists()) {
                    editProfile.setText("following");
                } else {
                    editProfile.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowers() {
        DatabaseReference followersReference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(profileid).child("followers");
        DatabaseReference followingReference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(profileid).child("following");

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

//    private void getPosts() {
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                int count = 0;
//                for (DataSnapshot data : snapshot.getChildren()) {
//                    Post post = data.getValue(Post.class);
//                    if (post != null && post.getAuthorID() != null && post.getAuthorID().equals(profileid)) {
//                        count += 1;
//                    }
//                }
//
//                posts.setText("" + count);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
}