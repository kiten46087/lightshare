package edu.neu.madcourse.share.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import edu.neu.madcourse.share.CommunityDetailActivity;
import edu.neu.madcourse.share.Model.Community;
import edu.neu.madcourse.share.R;

public class SingleCommunityAdapter extends RecyclerView.Adapter<SingleCommunityAdapter.ViewHolder> {

    private List<Community> communities;
    private Context context;

    public SingleCommunityAdapter(Context context, List<Community> communities) {
        this.context = context;
        this.communities = communities;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_community, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Community community = communities.get(position);
        // Set the title of the community.
        holder.name.setText(community.getName());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Communities")
                .child(community.getCommunityId());

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Community community = snapshot.getValue(Community.class);
                // Set the image of the community.
                Glide.with(context).load(community.getImage()).into(holder.imageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // When clicked go to the detail page of the community.
        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CommunityDetailActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("communityId", community.getCommunityId());
                intent.putExtra("creatorId", community.getCreator());
                context.startActivity(intent);
            }
        });

        // When clicked go to the detail page of the community.
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CommunityDetailActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("communityId", community.getCommunityId());
                intent.putExtra("creatorId", community.getCreator());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return communities.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            imageView = itemView.findViewById(R.id.image_view);


        }
    }
}
