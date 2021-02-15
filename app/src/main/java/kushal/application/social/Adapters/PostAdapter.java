package kushal.application.social.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
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
import java.util.HashMap;

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

        holder.setIsRecyclable(false);

        Post post = mlist.get(position);

        Picasso.get().load(post.getImage_url())
                .placeholder(R.drawable.placeholder).into(holder.post_image);
        holder.description.setText(post.getDescription());

        FirebaseDatabase.getInstance().getReference().child("users").child(post.getUser_id())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        holder.author.setText(snapshot.child("name").getValue().toString());
                        holder.user_name.setText(snapshot.child("user_name").getValue().toString());

                        if (TextUtils.isEmpty(snapshot.child("image_url").getValue().toString()))
                            holder.image_profile.setImageResource(R.drawable.ic_baseline_person);
                        else
                            Picasso.get().load(snapshot.child("image_url")
                                    .getValue().toString()).into(holder.image_profile);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        isLiked(post.getPost_id(), holder.like, holder.share);
        noOfLikes(post.getPost_id(), holder.noOfLikes);
        getComments(post.getPost_id(), holder.noOfComments);
        isSaved(post.getPost_id(), holder.save);

        holder.author.setOnClickListener(v -> holder.user_name.performClick());
        holder.image_profile.setOnClickListener(v -> holder.user_name.performClick());

        holder.user_name.setOnClickListener(v -> {
            Intent i = new Intent(mcontext, ProfileActivity.class);
            i.putExtra("user_id", post.getUser_id());
            mcontext.startActivity(i);
        });

        holder.share.setOnClickListener(v -> shareURL(post.getImage_url()));

        holder.like.setOnClickListener(v -> {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                    .child("likes").child(post.getPost_id());

            if (holder.like.getTag().toString().equals("Liked")) {
                ref.child(auth.getUid()).setValue(true);

                addNoti(post.getPost_id(), post.getUser_id(), "Liked your post", true);
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

                addNoti(post.getPost_id(), post.getUser_id(), "Liked your post", true);

//                full-on animation
                holder.like_heart.animate()
                        .scaleX(1.2f).scaleY(1.2f).setDuration(100);

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    holder.like_heart.animate()
                            .scaleX(0.01f).scaleY(0.01f).alpha(0.4f)
                            .setDuration(400);
                }, 250);

                handler.postDelayed(() -> {
                    holder.like_heart.setVisibility(View.GONE);
                    holder.like_heart.animate()
                            .scaleX(1f).scaleY(1f).alpha(1f)
                            .setDuration(0);
                }, 650);
            }
        }));

        holder.post_image.setOnLongClickListener(v -> {
            if (holder.post_image.getScaleType() == ImageView.ScaleType.CENTER_CROP)
                holder.post_image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            else
                holder.post_image.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            Toast.makeText(mcontext, "changed", Toast.LENGTH_SHORT).show();
            return true;
        });

        holder.comment.setOnClickListener(v -> holder.post_image.performClick());

        holder.noOfComments.setOnClickListener(v -> holder.post_image.performClick());
    }

    private void shareURL(String url) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, "Take a look:\n" + url + "\n\nShared via Social");
        mcontext.startActivity(Intent.createChooser(sharingIntent, "Share via"));

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

    private void getComments(String postId, final TextView text) {
        FirebaseDatabase.getInstance().getReference().child("comments")
                .child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 1) {
                    text.setVisibility(View.VISIBLE);
                    text.setText("View All " + dataSnapshot.getChildrenCount() + " Comments");
                } else if (dataSnapshot.getChildrenCount() == 1) {
                    text.setVisibility(View.VISIBLE);
                    text.setText("View " + dataSnapshot.getChildrenCount() + " Comment");
                } else
                    text.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isLiked(String post_id, ImageView like, ImageView share) {
        FirebaseDatabase.getInstance().getReference()
                .child("likes").child(post_id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child(auth.getUid()).exists()) {
                            like.setImageResource(R.drawable.ic_heart_fill);
                            share.setVisibility(View.VISIBLE);
                            like.setTag("Like");
                        } else {
                            like.setTag("Liked");
                            share.setVisibility(View.INVISIBLE);
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
        public ImageView save, share;
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
            like_heart = itemView.findViewById(R.id.like_heart);
            share = itemView.findViewById(R.id.share);

            user_name = itemView.findViewById(R.id.user_name);
            noOfLikes = itemView.findViewById(R.id.no_of_likes);
            author = itemView.findViewById(R.id.author);
            noOfComments = itemView.findViewById(R.id.no_of_comments);
            description = itemView.findViewById(R.id.description);
        }
    }
}
