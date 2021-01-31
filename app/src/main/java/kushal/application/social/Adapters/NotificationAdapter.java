package kushal.application.social.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import kushal.application.social.CommentActivity;
import kushal.application.social.Models.Notification;
import kushal.application.social.Models.Post;
import kushal.application.social.Models.User;
import kushal.application.social.ProfileActivity;
import kushal.application.social.R;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context mContext;
    private List<Notification> mNotifications;

    public NotificationAdapter(Context mContext, List<Notification> mNotifications) {
        this.mContext = mContext;
        this.mNotifications = mNotifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_item, parent, false);

        return new NotificationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Notification notification = mNotifications.get(position);

        getUser(holder.imageProfile, holder.username, notification.getUser_id());
        holder.comment.setText(notification.getText());

        Log.i("yoyo", String.valueOf(notification.getIs_post()));

        if (notification.getIs_post()) {
            holder.postImage.setVisibility(View.VISIBLE);
            getPostImage(holder.postImage, notification.getPost_id());
        } else{
            holder.postImage.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(mContext, ProfileActivity.class);
            i.putExtra("user_id", notification.getUser_id());
            mContext.startActivity(i);
        });

        holder.username.setOnClickListener(v -> holder.itemView.performClick());
        holder.imageProfile.setOnClickListener(v -> holder.itemView.performClick());

    }

    @Override
    public int getItemCount() {
        return mNotifications.size();
    }

    private void getPostImage(final ImageView imageView, String postId) {
        FirebaseDatabase.getInstance().getReference()
                .child("posts").child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);
                Picasso.get().load(post.getImage_url()).placeholder(R.drawable.placeholder).into(imageView);

                imageView.setOnClickListener(v -> {
                    Intent i = new Intent(mContext, CommentActivity.class);
                    i.putExtra("user_id", post.getUser_id());
                    i.putExtra("post_id", post.getPost_id());
                    i.putExtra("description", post.getDescription());
                    i.putExtra("post_image_url", post.getImage_url());
                    mContext.startActivity(i);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUser(final ImageView imageView, final TextView textView, String userId) {
        FirebaseDatabase.getInstance().getReference()
                .child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (TextUtils.isEmpty(user.getImage_url()))
                    imageView.setImageResource(R.drawable.ic_baseline_person);
                else Picasso.get().load(user.getImage_url()).into(imageView);

                textView.setText(user.getUser_name());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageProfile;
        public ImageView postImage;
        public TextView username;
        public TextView comment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageProfile = itemView.findViewById(R.id.image_profile);
            postImage = itemView.findViewById(R.id.post_image);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
        }
    }

}