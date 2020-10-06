package edu.neu.madcourse.share.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.neu.madcourse.share.Model.User;
import edu.neu.madcourse.share.R;
import edu.neu.madcourse.share.UserPageActivity;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;

    private boolean isFragment;

    private FirebaseUser firebaseUser;

    public UserAdapter(Context mContext, List<User> mUsers, boolean isFragment) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isFragment = isFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, viewGroup, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final User user = mUsers.get(position);

        viewHolder.btnFollow.setVisibility(View.VISIBLE);

        viewHolder.username.setText(user.getUsername());
        viewHolder.fullname.setText(user.getFullname());
        Glide.with(mContext).load(user.getImageurl()).into(viewHolder.imageProfile);
        isFollowing(user.getId(), viewHolder.btnFollow);

        if (user.getId().equals(firebaseUser.getUid())) {
            viewHolder.btnFollow.setVisibility(View.GONE);
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, UserPageActivity.class);
                intent.putExtra("userId", user.getId());
                mContext.startActivity(intent);
            }
        });

        viewHolder.btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewHolder.btnFollow.getTag().equals("not followed")) {
                    FirebaseDatabase.getInstance().getReference()
                            .child("Follow")
                            .child(firebaseUser.getUid())
                            .child("following")
                            .child(user.getId())
                            .setValue(true);
                    FirebaseDatabase.getInstance().getReference()
                            .child("Follow")
                            .child(user.getId())
                            .child("followers")
                            .child(firebaseUser.getUid())
                            .setValue(true);

                    addNotifications(user.getId());
                } else {
                    FirebaseDatabase.getInstance().getReference()
                            .child("Follow")
                            .child(firebaseUser.getUid())
                            .child("following")
                            .child(user.getId())
                            .removeValue();
                    FirebaseDatabase.getInstance().getReference()
                            .child("Follow")
                            .child(user.getId())
                            .child("followers")
                            .child(firebaseUser.getUid())
                            .removeValue();
                }
            }
        });
    }

    private void addNotifications(String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Notifications").child(userId);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userId", firebaseUser.getUid());
        hashMap.put("text", "started following you");
        hashMap.put("postId", "");
        hashMap.put("isPost", false);

        reference.push().setValue(hashMap);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public TextView fullname;
        public CircleImageView imageProfile;
//        public Button btnFollow;
        public ImageView btnFollow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            fullname = itemView.findViewById(R.id.fullname);
            imageProfile = itemView.findViewById(R.id.image_profile);
            btnFollow = itemView.findViewById(R.id.btn_follow);
        }
    }

    private void isFollowing(final String userid, final ImageView button) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow")
                .child(firebaseUser.getUid())
                .child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(userid).exists()) {
//                    button.setText("following");
                    button.setImageResource(R.drawable.ic_person);
                    button.setTag("followed");

                } else {
//                    button.setText("follow");
                    button.setImageResource(R.drawable.ic_addperson);
                    button.setTag("not followed");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
