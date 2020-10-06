package edu.neu.madcourse.share;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
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

import edu.neu.madcourse.share.Model.Community;
import edu.neu.madcourse.share.Model.Post;

public class PostInCommunityActivity extends AppCompatActivity {

    Uri imageUri;
    String myUrl = "";
    String communityId = "";
    String communityName = "";
    StorageTask uploadTask;
    StorageReference storageReference;

    ImageView close, image_added;
    TextView post;
    TextView illustration;
    EditText title;
    EditText content;
    String curUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_in_community);

        Intent intent = getIntent();
        communityId = intent.getStringExtra("communityID");


        close = findViewById(R.id.close);
        image_added = findViewById(R.id.added_image);
        post = findViewById(R.id.post);
        title = findViewById(R.id.Title);
        content = findViewById(R.id.content);
        illustration = findViewById(R.id.illustration);
        setIllustration();

        // Get the current user.
        curUser = FirebaseAuth.getInstance().getCurrentUser().getUid();


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

        image_added.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(PostInCommunityActivity.this);
            }
        });

    }

    private void setIllustration() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Communities").child(communityId);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Community community = snapshot.getValue(Community.class);
                communityName = community.getName();
                illustration.setText("Posting to: " + communityName);
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
                                || TextUtils.isEmpty(title.getText().toString())
                                || TextUtils.isEmpty(communityName)) {

                            progressDialog.dismiss();
                            Toast.makeText(PostInCommunityActivity.this, "All Fields are required", Toast.LENGTH_SHORT).show();
                        } else {
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

                            String postId = reference.push().getKey();

                            final Post newPost = new Post();
                            newPost.setPostID(postId);
                            newPost.setAuthorID(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            newPost.setPostContent(content.getText().toString());
                            newPost.setPostIMG(myUrl);
                            newPost.setTitle(title.getText().toString());
                            newPost.setCommunity(communityName);

                            reference.child(postId).setValue(newPost);

                            progressDialog.dismiss();

                            finish();
                        }
                    } else {
                        Toast.makeText(PostInCommunityActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostInCommunityActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No Image Selected!", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();

            image_added.setImageURI(imageUri);
        } else {
//            Toast.makeText(this, "Something's gone wrong!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}