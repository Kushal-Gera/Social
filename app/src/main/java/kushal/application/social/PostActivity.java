package kushal.application.social;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.hendraanggrian.appcompat.socialview.Hashtag;
import com.hendraanggrian.appcompat.widget.HashtagArrayAdapter;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.List;

public class PostActivity extends AppCompatActivity {
    private Uri imageUri;
    private String imageUrl;

    private ImageView close;
    private ImageView imageAdded;
    private TextView post;
    SocialAutoCompleteTextView description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        close = findViewById(R.id.close);
        imageAdded = findViewById(R.id.image_added);
        post = findViewById(R.id.post);
        description = findViewById(R.id.description);

        close.setOnClickListener(v -> {
            finish();
        });

        post.setOnClickListener(v -> upload());

        CropImage.activity().start(PostActivity.this);
    }

    private void upload() {

        final ProgressDialog pd = new ProgressDialog(this, R.style.progress_dialog_theme);
        pd.setMessage("Please Wait...");
        pd.setTitle("Posting");
        pd.setCancelable(false);
        pd.show();

        if (imageUri != null) {
            final StorageReference filePath = FirebaseStorage.getInstance()
                    .getReference("posts")
                    .child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            StorageTask uploadtask = filePath.putFile(imageUri);
            uploadtask.continueWithTask((Continuation) task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return filePath.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                Uri downloadUri = task.getResult();
                imageUrl = downloadUri.toString();

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                FirebaseUser auth = FirebaseAuth.getInstance().getCurrentUser();
                String postId = ref.child("posts").push().getKey();

                HashMap<String, Object> map = new HashMap<>();
                map.put("post_id", postId);
                map.put("image_url", imageUrl);
                map.put("description", description.getText().toString());
                map.put("user_id", auth.getUid());

                //pushed to main-post column
                ref.child("posts").child(postId).setValue(map);
                //pushed to main-user-post column
                ref.child("users").child(auth.getUid()).child("posts").child(postId).setValue(map);

                //pushed to main-hashtags column
                DatabaseReference mHashTagRef = ref.child("hashtags");
                List<String> hashTags = description.getHashtags();
                if (!hashTags.isEmpty()) {
                    for (String tag : hashTags) {
                        map.clear();

                        map.put("tag", tag.toLowerCase());
                        map.put("post_id", postId);

                        mHashTagRef.child(tag.toLowerCase()).child(postId).setValue(map);
                    }
                }

                pd.dismiss();
                finish();
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            Toast.makeText(this, "No image was selected!", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(this.getContentResolver().getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            imageAdded.setImageURI(imageUri);
        } else {
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        final ArrayAdapter<Hashtag> hashtagAdapter = new HashtagArrayAdapter<>(getApplicationContext());

        FirebaseDatabase.getInstance().getReference().child("HashTags").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    hashtagAdapter.add(new Hashtag(snapshot.getKey(), (int) snapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        description.setHashtagAdapter(hashtagAdapter);
    }
}