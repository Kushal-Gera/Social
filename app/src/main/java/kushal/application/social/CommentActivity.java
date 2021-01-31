package kushal.application.social;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kushal.application.social.Adapters.CommentAdapter;
import kushal.application.social.Models.Comment;

public class CommentActivity extends AppCompatActivity {

    private RecyclerView recycler_view_comments;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    ScrollView scrollview;

    private EditText addComment;
    private CircleImageView imageProfile;
    private TextView user_nameee;
    ImageView send, post_image, close;

    private String post_id;
    private String profile_img_url = "", my_profile_img;
    private String user_id;
    private String string_description, post_image_url;

    FirebaseUser auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Intent intent = getIntent();
        post_id = intent.getStringExtra("post_id");
        user_id = intent.getStringExtra("user_id");
        string_description = intent.getStringExtra("description");
        post_image_url = intent.getStringExtra("post_image_url");

        recycler_view_comments = findViewById(R.id.recycler_view_comments);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        llm.setReverseLayout(true);
        recycler_view_comments.setLayoutManager(llm);

        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList, post_id);
        recycler_view_comments.setAdapter(commentAdapter);


        scrollview = findViewById(R.id.scrollview);
        addComment = findViewById(R.id.add_comment);
        close = findViewById(R.id.close);
        post_image = findViewById(R.id.post_image);
        imageProfile = findViewById(R.id.image_profile);
        user_nameee = findViewById(R.id.user_nameee);
        send = findViewById(R.id.send);


        Picasso.get().load(post_image_url).into(post_image);
        auth = FirebaseAuth.getInstance().getCurrentUser();

        send.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(addComment.getText().toString()))
                putComment();
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        });

        user_nameee.setOnClickListener(v->{
            Intent i = new Intent(CommentActivity.this, ProfileActivity.class);
            i.putExtra("user_id", user_id);
            startActivity(i);
        });

        close.setOnClickListener(v -> finish());

        MyTask myTask = new MyTask();
        myTask.execute();

    }

    private void putComment() {

        HashMap<String, Object> map = new HashMap<>();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference().child("comments").child(post_id);

        String id = ref.push().getKey();

        map.put("comment", addComment.getText().toString());
        map.put("user_id", auth.getUid());
        map.put("id", id);
        map.put("image_url", my_profile_img);

        addComment.setText("");

        ref.child(id).setValue(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                addNoti(post_id, user_id, "Commented on your post", true);
            }else{
                Toast.makeText(CommentActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void addNoti(String post_id, String user_id, String text, Boolean isPost) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("post_id", post_id);
        map.put("user_id", auth.getUid());
        map.put("text", text);
        map.put("is_post", isPost);

        FirebaseDatabase.getInstance().getReference()
                .child("notifications").child(user_id).push().setValue(map);
    }

    public class MyTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {

//            getUserImage();
            FirebaseDatabase.getInstance().getReference().child("comments")
                    .child(post_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    commentList.clear();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Comment comment = snapshot.getValue(Comment.class);
                        commentList.add(comment);
                    }

                    commentAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

//            getComment();
            FirebaseDatabase.getInstance().getReference().child("comments")
                    .child(post_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    commentList.clear();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Comment comment = snapshot.getValue(Comment.class);
                        commentList.add(comment);
                    }

                    commentAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            FirebaseDatabase.getInstance().getReference().child("users")
                    .child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    profile_img_url = snapshot.child("image_url").getValue().toString();

                    user_nameee.setText(snapshot.child("user_name").getValue().toString());
                    if (!TextUtils.isEmpty(profile_img_url))
                        Picasso.get().load(profile_img_url).into(imageProfile);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            FirebaseDatabase.getInstance().getReference().child("users")
                    .child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    my_profile_img = snapshot.child("image_url").getValue().toString();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            return null;
        }
    }


}