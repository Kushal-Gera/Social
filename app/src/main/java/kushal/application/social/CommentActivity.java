package kushal.application.social;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.hendraanggrian.appcompat.widget.SocialTextView;
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
    private TextView user_name, author;
    SocialTextView description;
    ImageView send;

    private String post_id;
    private String user_id;
    private String string_description;

    FirebaseUser auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Intent intent = getIntent();
        post_id = intent.getStringExtra("post_id");
        user_id = intent.getStringExtra("user_id");
        string_description = intent.getStringExtra("description");

        description.setText(string_description);


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
        imageProfile = findViewById(R.id.image_profile);
        user_name = findViewById(R.id.user_name);
        author = findViewById(R.id.author);
        description = findViewById(R.id.description);
        send = findViewById(R.id.send);

        auth = FirebaseAuth.getInstance().getCurrentUser();

        getUserImage();

        send.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(addComment.getText().toString()))
                putComment();
        });

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

        addComment.setText("");

        ref.child(id).setValue(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(CommentActivity.this, "Comment added!", Toast.LENGTH_SHORT).show();
            } else {
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
                author.setText(user.getName());

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