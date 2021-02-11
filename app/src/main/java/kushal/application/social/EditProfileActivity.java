package kushal.application.social;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import kushal.application.social.Models.User;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView close;
    private CircleImageView imageProfile;
    private TextView save;
    private ImageView changePhoto;
    private TextInputLayout fullname, username, email, bio;
    String new_img_url = "";

    private FirebaseUser auth;

    private Uri mImageUri;
    private StorageTask uploadTask;
    private StorageReference storageRef;

    String first_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        first_time = getIntent().getStringExtra("first_time");

        close = findViewById(R.id.close);
        imageProfile = findViewById(R.id.image_profile);
        save = findViewById(R.id.done);
        changePhoto = findViewById(R.id.change_photo);
        fullname = findViewById(R.id.fullname);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        bio = findViewById(R.id.bio);

        auth = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference().child("uploads");

        FirebaseDatabase.getInstance().getReference()
                .child("users").child(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("name").exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    fullname.getEditText().setText(user.getName());
                    username.getEditText().setText(user.getUser_name());
                    bio.getEditText().setText(user.getBio());
                    email.getEditText().setText(user.getEmail());
                    if (!TextUtils.isEmpty(user.getImage_url()))
                        Picasso.get().load(user.getImage_url()).into(imageProfile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        close.setOnClickListener(v -> onBackPressed());

        changePhoto.setOnClickListener(v ->
                CropImage.activity().setCropShape(CropImageView.CropShape.OVAL)
                        .start(EditProfileActivity.this));

        imageProfile.setOnClickListener(v ->
                CropImage.activity().setCropShape(CropImageView.CropShape.OVAL)
                        .start(EditProfileActivity.this));

        save.setOnClickListener(v -> {
            if (TextUtils.isEmpty(fullname.getEditText().getText()))
                fullname.getEditText().setError("Name required");
            else if (TextUtils.isEmpty(username.getEditText().getText()))
                username.getEditText().setError("Username required");
            else updateProfile();
        });
    }

    private void updateProfile() {
        Toast.makeText(this, "Saving", Toast.LENGTH_SHORT).show();

        HashMap<String, Object> map = new HashMap<>();
        map.put("name", fullname.getEditText().getText().toString());
        map.put("user_name", username.getEditText().getText().toString());
        map.put("email", email.getEditText().getText().toString());
        map.put("id", auth.getUid());
        map.put("bio", bio.getEditText().getText().toString());
        if (("first_time").equals(first_time) || (!TextUtils.isEmpty(new_img_url)))
            map.put("image_url", new_img_url);

        Log.i("first_time", "new_img_url = " + new_img_url + ",  first_time = " + first_time);

        FirebaseDatabase.getInstance().getReference()
                .child("users").child(auth.getUid())
                .updateChildren(map).addOnCompleteListener(task -> finish());

    }

    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(this, R.style.progress_dialog_theme);
        pd.setTitle("Uploading Image");
        pd.setMessage("Please wait...");
        pd.setCancelable(false);
        pd.show();

        if (mImageUri != null) {
            final StorageReference fileRef = storageRef.child(System.currentTimeMillis() + ".jpeg");

            uploadTask = fileRef.putFile(mImageUri);
            uploadTask.continueWithTask((Continuation) task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                return fileRef.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    String url = downloadUri.toString();

                    new_img_url = url;
                    Picasso.get().load(url).into(imageProfile);
                    pd.dismiss();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Image upload failed!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            mImageUri = result.getUri();

            uploadImage();
        }
    }

    @Override
    public void onBackPressed() {
        if (TextUtils.isEmpty(first_time))
            super.onBackPressed();
        else
            Toast.makeText(this, "Fill and save all required fields.", Toast.LENGTH_LONG).show();
    }
}