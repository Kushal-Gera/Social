package kushal.application.social;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kushal.application.social.Adapters.PhotoAdapter;
import kushal.application.social.Models.Post;
import kushal.application.social.Models.User;

public class ProfileActivity extends AppCompatActivity {


    private PhotoAdapter postAdapterSaved;
    private List<Post> mySavedList;

    private RecyclerView recyclerview_mypictures, recycler_view_saved;
    private PhotoAdapter photoAdapter;
    private List<Post> myPhotoList;

    private CircleImageView imageProfile;
    private TextView followers;
    private TextView following;
    private TextView posts;
    private TextView name;
    private TextView bio;
    private TextView username;

    private ImageView myPictures, close;
    private ImageView savedPictures, options;

    private TextView editProfile;
    ProgressBar progress_bar;

    String user_id;
    FirebaseUser auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        user_id = getIntent().getStringExtra("user_id");
        auth = FirebaseAuth.getInstance().getCurrentUser();

        imageProfile = findViewById(R.id.image_profile);
        followers = findViewById(R.id.followers);
        following = findViewById(R.id.following);
        posts = findViewById(R.id.posts);
        close = findViewById(R.id.close);
        name = findViewById(R.id.name);
        bio = findViewById(R.id.bio);
        username = findViewById(R.id.username);
        myPictures = findViewById(R.id.my_pictures);
        savedPictures = findViewById(R.id.saved_pictures);
        editProfile = findViewById(R.id.edit_profile);
        progress_bar = findViewById(R.id.progress_bar);
        options = findViewById(R.id.options);


        recyclerview_mypictures = findViewById(R.id.recyclerview_mypictures);
        recyclerview_mypictures.setHasFixedSize(true);
        recyclerview_mypictures.setLayoutManager(new GridLayoutManager(this, 3));
        myPhotoList = new ArrayList<>();
        photoAdapter = new PhotoAdapter(this, myPhotoList);
        photoAdapter.setHasStableIds(true);
        recyclerview_mypictures.setAdapter(photoAdapter);

        recycler_view_saved = findViewById(R.id.recycler_view_saved);
        recycler_view_saved.setHasFixedSize(true);
        recycler_view_saved.setLayoutManager(new GridLayoutManager(this, 3));
        mySavedList = new ArrayList<>();
        postAdapterSaved = new PhotoAdapter(this, mySavedList);
        recycler_view_saved.setAdapter(postAdapterSaved);


        close.setOnClickListener(v -> finish());
        options.setOnClickListener(v -> startActivity(new Intent(this, OptionsActivity.class)));
        progress_bar.setVisibility(View.VISIBLE);


        new MyTask().execute();

        if (user_id.equals(auth.getUid())) {
            editProfile.setText("Edit Profile");
            getSavedPosts();
            options.setVisibility(View.VISIBLE);
        } else {
            checkFollowingStatus();
            savedPictures.setVisibility(View.GONE);
        }

        recyclerview_mypictures.setVisibility(View.VISIBLE);
        recycler_view_saved.setVisibility(View.GONE);

        myPictures.setOnClickListener(v -> {
            recyclerview_mypictures.setVisibility(View.VISIBLE);
            myPictures.setBackground(getResources().getDrawable(R.drawable.gray_fill_bg));

            savedPictures.setBackgroundColor(getResources().getColor(R.color.white));
            recycler_view_saved.setVisibility(View.GONE);
        });

        savedPictures.setOnClickListener(v -> {
            recyclerview_mypictures.setVisibility(View.GONE);
            myPictures.setBackgroundColor(getResources().getColor(R.color.white));

            savedPictures.setBackground(getResources().getDrawable(R.drawable.gray_fill_bg));
            recycler_view_saved.setVisibility(View.VISIBLE);
        });

        editProfile.setOnClickListener(v -> {
            String btnText = editProfile.getText().toString();

            if (btnText.equals("Edit Profile")) {
                startActivity(new Intent(this, EditProfileActivity.class));
            } else {
                if (btnText.equals("Follow")) {
                    FirebaseDatabase.getInstance().getReference()
                            .child("followings").child(auth.getUid())
                            .child(user_id).setValue(true);

                    FirebaseDatabase.getInstance().getReference()
                            .child("followers").child(user_id).child(auth.getUid())
                            .setValue(true);

                    addNoti("", user_id, "Started following you", false);
                } else {
                    Intent i = new Intent(this, ChatActivity.class);
                    i.putExtra("user_id", user_id);
                    startActivity(i);
                }
            }
        });

    }

    private void addNoti(String post_id, String user_id, String text, Boolean isPost) {
        if (auth.getUid().equals(user_id)) return;

        HashMap<String, Object> map = new HashMap<>();
        map.put("post_id", post_id);
        map.put("user_id", auth.getUid());
        map.put("text", text);
        map.put("is_post", isPost);

        FirebaseDatabase.getInstance().getReference()
                .child("notifications").child(user_id).push().setValue(map);
    }

    private void getSavedPosts() {

        final List<String> savedIds = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference().child("saved").child(auth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot datasnapshot) {

                        for (DataSnapshot snapshot : datasnapshot.getChildren())
                            savedIds.add(snapshot.getKey().toString());

                        FirebaseDatabase.getInstance().getReference()
                                .child("posts").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                mySavedList.clear();

                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    if (savedIds.contains(dataSnapshot.getKey().toString()))
                                        mySavedList.add(dataSnapshot.getValue(Post.class));
                                }

                                postAdapterSaved.notifyDataSetChanged();
                                Collections.reverse(mySavedList);
                                progress_bar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void checkFollowingStatus() {

        FirebaseDatabase.getInstance().getReference().child("followers")
                .child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(auth.getUid()).exists()) {
                    editProfile.setText("Message");
                    editProfile.setTextColor(getResources().getColor(R.color.black));
                    editProfile.setBackground(getResources().getDrawable(R.drawable.gray_btn_bg));
                } else {
                    editProfile.setText("Follow");
                    editProfile.setTextColor(getResources().getColor(R.color.white));
                    editProfile.setBackground(getResources().getDrawable(R.drawable.blue_button_bg));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public class MyTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {

//            userInfo();
            FirebaseDatabase.getInstance().getReference()
                    .child("users").child(user_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);

                    if (!TextUtils.isEmpty(user.getImage_url()))
                        Picasso.get().load(user.getImage_url()).into(imageProfile);
                    username.setText(user.getUser_name());
                    name.setText(user.getName());
                    bio.setText(user.getBio());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


//            getFollowersAndFollowingCount();
            FirebaseDatabase.getInstance().getReference().child("followers").child(user_id)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            followers.setText("" + snapshot.getChildrenCount());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            FirebaseDatabase.getInstance().getReference().child("followings").child(user_id)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            following.setText("" + snapshot.getChildrenCount());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


//            getPostCount();
            FirebaseDatabase.getInstance().getReference().child("users").child(user_id)
                    .child("posts").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    posts.setText("" + dataSnapshot.getChildrenCount());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


//            myPhotos();
            FirebaseDatabase.getInstance().getReference().child("users").child(user_id)
                    .child("posts").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    myPhotoList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        myPhotoList.add(snapshot.getValue(Post.class));
                    }

                    Collections.reverse(myPhotoList);
                    photoAdapter.notifyDataSetChanged();
                    progress_bar.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            return null;
        }
    }

}