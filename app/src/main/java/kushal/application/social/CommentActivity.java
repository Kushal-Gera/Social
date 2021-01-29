package kushal.application.social;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
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
import kushal.application.social.Models.User;

public class CommentActivity extends AppCompatActivity {

    private RecyclerView recycler_view_comments;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    private EditText addComment;
    private CircleImageView imageProfile;
    private TextView user_name;
    ImageView send, post_image, close;

    private String post_id;
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
        recycler_view_comments.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        llm.setReverseLayout(true);
        recycler_view_comments.setLayoutManager(llm);

        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList, post_id);
        recycler_view_comments.setAdapter(commentAdapter);


        addComment = findViewById(R.id.add_comment);
        close = findViewById(R.id.close);
        post_image = findViewById(R.id.post_image);
        imageProfile = findViewById(R.id.image_profile);
        user_name = findViewById(R.id.user_name);
        send = findViewById(R.id.send);


        Picasso.get().load(post_image_url).into(post_image);
        auth = FirebaseAuth.getInstance().getCurrentUser();

        getUserImage();

        send.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(addComment.getText().toString()))
                putComment();
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        });
        close.setOnClickListener(v -> finish());

        getComment();
    }

    private void getComment() {

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

    }

    private void putComment() {

        HashMap<String, Object> map = new HashMap<>();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference().child("comments").child(post_id);

        String id = ref.push().getKey();

        map.put("comment", addComment.getText().toString());
        map.put("user_id", auth.getUid());
        map.put("id", id);

        addComment.setText("");

        ref.child(id).setValue(map).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(CommentActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getUserImage() {

        FirebaseDatabase.getInstance().getReference()
                .child("users").child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                user_name.setText(user.getUser_name());

                if (TextUtils.isEmpty(user.getImage_url())) {
                    imageProfile.setImageResource(R.drawable.ic_baseline_person);
                } else {
                    Picasso.get().load(user.getImage_url()).into(imageProfile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}