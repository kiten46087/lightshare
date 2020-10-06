package edu.neu.madcourse.share;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.neu.madcourse.share.Model.Community;
import edu.neu.madcourse.share.Model.Post;

public class PostActivity extends AppCompatActivity {

    Uri imageUri;
    String myUrl = "";
    StorageTask uploadTask;
    StorageReference storageReference;

    ImageView close, addedImage;
    TextView post;
    EditText title;
    EditText content;
    Spinner communitySpinner;
    Button create;
    String[] communities;
    String curUser;
    Set<String> storedCommunities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        close = findViewById(R.id.close);
        addedImage = findViewById(R.id.added_image);
        post = findViewById(R.id.post);
        title = findViewById(R.id.Title);
        content = findViewById(R.id.content);
        communitySpinner = findViewById(R.id.community);
        create = findViewById(R.id.create);

        // Get the current user.
        curUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        storedCommunities = new HashSet<>();

        getSubscribeCommunities(curUser);

        // Choose from the communities.
        setSpinners();

        storageReference = FirebaseStorage.getInstance().getReference("posts");

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        addedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(PostActivity.this);
            }
        });


        //create new community
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostActivity.this, CreateCommunityActivity.class));
            }
        });

    }

    // Get the communities which the curUser subscribe.
    private void getSubscribeCommunities(String curUser) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Subscribe").child(curUser);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    String tempId = data.getKey();
                    storedCommunities.add(tempId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Set the spinners.
    private void setSpinners() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Communities");
        final List<String> list = new ArrayList<>();

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    if (storedCommunities.contains(data.getValue(Community.class).getCommunityId())) {
                        list.add(data.getValue(Community.class).getName());
                    }
                }

                // Convert the list to the array.
                communities = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    communities[i] = list.get(i);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(PostActivity.this, android.R.layout.simple_spinner_item, communities);
                communitySpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void setPost(final String communityName) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Communities");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    Community community = data.getValue(Community.class);

                    if (community.getName().equals(communityName)) {
                        community.getPosts().add(community.getCommunityId());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting");
        progressDialog.show();

        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(
                    System.currentTimeMillis() + "." + getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();

                        if (TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                || TextUtils.isEmpty(content.getText().toString())
                                || TextUtils.isEmpty(myUrl)
                                || TextUtils.isEmpty(title.getText().toString())) {
                            progressDialog.dismiss();
                            Toast.makeText(PostActivity.this, "All Fields are required", Toast.LENGTH_SHORT).show();
                        } else {
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

                            String postId = reference.push().getKey();

                            final Post newPost = new Post();
                            newPost.setPostID(postId);
                            newPost.setAuthorID(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            newPost.setPostContent(content.getText().toString());
                            newPost.setPostIMG(myUrl);
                            newPost.setTitle(title.getText().toString());
                            newPost.setCommunity(communitySpinner.getSelectedItem().toString());

                            reference.child(postId).setValue(newPost);

                            progressDialog.dismiss();

                            finish();
                        }
                    } else {
                        Toast.makeText(PostActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No Image Selected!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();

            addedImage.setImageURI(imageUri);
        } else {
//            Toast.makeText(this, "Something's gone wrong!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}