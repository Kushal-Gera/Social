package kushal.application.social.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.appcompat.widget.SocialTextView;
import com.pedromassango.doubleclick.DoubleClick;
import com.pedromassango.doubleclick.DoubleClickListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kushal.application.social.CommentActivity;
import kushal.application.social.Models.Post;
import kushal.application.social.ProfileActivity;
import kushal.application.social.R;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.Viewholder> {

    ArrayList<Post> mlist;
    Context mcontext;
    FirebaseUser auth;

    public PostAdapter(Context mcontext, ArrayList<Post> mlist) {
        this.mlist = mlist;
        this.mcontext = mcontext;
        auth = FirebaseAuth.getInstance().getCurrentUser();
    }


    @NonNull
    @Override
    public PostAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext).inflate(R.layout.post_item, parent, false);
        return new PostAdapter.Viewholder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull PostAdapter.Viewholder holder, int position) {

        Post post = mlist.get(position);

        Picasso.get().load(post.getImage_url())
                .placeholder(R.drawable.person_fill).into(holder.post_image);
        holder.description.setText(post.getDescription());


        FirebaseDatabase.getInstance().getReference().child("users").child(post.getUser_id())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        holder.author.setText(snapshot.child("name").getValue().toString());
                        holder.user_name.setText(snapshot.child("user_name").getValue().toString());

                        if (!snapshot.child("image_url").getValue().toString().isEmpty())
                            Picasso.get().load(snapshot.child("image_url").getValue().toString())
                                    .placeholder(R.drawable.person_fill).into(holder.image_profile);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        isLiked(post.getPost_id(), holder.like);
        noOfLikes(post.getPost_id(), holder.noOfLikes);
        getComments(post.getPost_id(), holder.noOfComments);
        isSaved(post.getPost_id(), holder.save);

        holder.author.setOnClickListener(v -> holder.user_name.performClick());

        holder.user_name.setOnClickListener(v -> {
            Intent i = new Intent(mcontext, ProfileActivity.class);
            i.putExtra("user_id", post.getUser_id());
            mcontext.startActivity(i);
        });

        holder.like.setOnClickListener(v -> {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                    .child("likes").child(post.getPost_id());

            if (holder.like.getTag().toString().equals("Liked")) {
                ref.child(auth.getUid()).setValue(true);
            } else {
                ref.child(auth.getUid()).removeValue();
            }
        });

        holder.save.setOnClickListener(v -> {
            if (v.getTag().equals("Saved")) {
                FirebaseDatabase.getInstance().getReference()
                        .child("saved").child(auth.getUid()).child(post.getPost_id()).setValue(true);
            } else {
                FirebaseDatabase.getInstance().getReference()
                        .child("saved").child(auth.getUid()).child(post.getPost_id()).removeValue();
            }
        });

        holder.post_image.setOnClickListener(new DoubleClick(new DoubleClickListener() {
            @Override
            public void onSingleClick(View view) {

                Intent i = new Intent(mcontext, CommentActivity.class);
                i.putExtra("user_id", post.getUser_id());
                i.putExtra("post_id", post.getPost_id());
                i.putExtra("description", post.getDescription());
                i.putExtra("post_image_url", post.getImage_url());
                mcontext.startActivity(i);
            }

            @Override
            public void onDoubleClick(View view) {

                holder.like_heart.setVisibility(View.VISIBLE);
                FirebaseDatabase.getInstance().getReference().child("likes")
                        .child(post.getPost_id()).child(auth.getUid()).setValue(true);

                Handler handler = new Handler();

                handler.postDelayed(() -> {
                    holder.like_heart.animate()
                            .scaleX(0.01f).scaleY(0.01f).alpha(0.4f)
                            .setDuration(500);
                }, 200);

                handler.postDelayed(() -> {
                    holder.like_heart.setVisibility(View.GONE);
                    holder.like_heart.animate()
                            .scaleX(1f).scaleY(1f).alpha(1f)
                            .setDuration(0);
                }, 750);
            }
        }));

        holder.comment.setOnClickListener(v -> holder.post_image.performClick());

        holder.noOfComments.setOnClickListener(v -> holder.post_image.performClick());
    }

    private void getComments(String postId, final TextView text) {
        FirebaseDatabase.getInstance().getReference().child("comments")
                .child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 1)
                    text.setText("View All " + dataSnapshot.getChildrenCount() + " Comments");
                else if (dataSnapshot.getChildrenCount() == 1)
                    text.setText("View " + dataSnapshot.getChildrenCount() + " Comment");
                else
                    text.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isLiked(String post_id, ImageView like) {
        FirebaseDatabase.getInstance().getReference()
                .child("likes").child(post_id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child(auth.getUid()).exists()) {
                            like.setImageResource(R.drawable.ic_heart_fill);
                            like.setTag("Like");
                        } else {
                            like.setTag("Liked");
                            like.setImageResource(R.drawable.heart_plain);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void isSaved(String post_id, ImageView save) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("saved").child(auth.getUid());

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(post_id).exists()) {
                    save.setImageResource(R.drawable.bookmark_fill);
                    save.setTag("Save");
                } else {
                    save.setImageResource(R.drawable.bookmark_plain);
                    save.setTag("Saved");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void noOfLikes(String post_id, TextView noOfLikes) {
        FirebaseDatabase.getInstance().getReference().child("likes")
                .child(post_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                noOfLikes.setText(snapshot.getChildrenCount() + " likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mlist.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {

        public ImageView image_profile;
        public ImageView post_image;
        public ImageView like;
        public ImageView comment;
        public ImageView save;
        public ImageView more, like_heart;

        public TextView user_name;
        public TextView noOfLikes;
        public TextView author;
        public TextView noOfComments;
        SocialTextView description;

        public Viewholder(@NonNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.image_profile);
            post_image = itemView.findViewById(R.id.post_image);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            save = itemView.findViewById(R.id.save);
            more = itemView.findViewById(R.id.more);

            user_name = itemView.findViewById(R.id.user_name);
            noOfLikes = itemView.findViewById(R.id.no_of_likes);
            author = itemView.findViewById(R.id.author);
            noOfComments = itemView.findViewById(R.id.no_of_comments);
            description = itemView.findViewById(R.id.description);

            like_heart = itemView.findViewById(R.id.like_heart);
        }
    }
}
