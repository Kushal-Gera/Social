package kushal.application.social.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import kushal.application.social.CommentActivity;
import kushal.application.social.Models.Post;
import kushal.application.social.R;

import static java.lang.Math.max;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

    private Context mContext;
    private List<Post> mPosts;
    FirebaseUser auth;
    long cnt = 0;

    public PhotoAdapter(Context mContext, List<Post> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
        auth = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.photo_item, parent, false);
        return new PhotoAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Post post = mPosts.get(position);

        Picasso.get().load(post.getImage_url()).fit().centerCrop()
                .placeholder(R.drawable.placeholder).into(holder.postImage);

        holder.postImage.setOnClickListener(v -> {
            Intent i = new Intent(mContext, CommentActivity.class);
            i.putExtra("user_id", post.getUser_id());
            i.putExtra("post_id", post.getPost_id());
            i.putExtra("description", post.getDescription());
            i.putExtra("post_image_url", post.getImage_url());
            mContext.startActivity(i);
        });

        if (post.getUser_id().equals(auth.getUid()))
            holder.postImage.setOnLongClickListener(v -> {

                Log.i("got in", "here");

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.progress_dialog_theme);

                builder.setTitle("Do you want to delete it permanently ?");
                builder.setMessage("It can never be recovered !");
                builder.setPositiveButton("Yes", (dialog, which) -> deleteIt(post));
                builder.setNegativeButton("no", (dialog, which) -> dialog.dismiss());
                builder.show();

                return true;
            });
    }

    void deleteIt(Post post) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Log.i("got in", "here again");

//        from notifications
        cnt = 0;
        ref.child("notifications").child(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child("post_id").exists() &&
                            !TextUtils.isEmpty(dataSnapshot.child("post_id").getValue().toString()))
                        if (dataSnapshot.child("post_id").getValue().toString().equals(post.getPost_id())) {
                            Log.i("post_id", dataSnapshot.getRef().toString());
                            dataSnapshot.getRef().removeValue();
                            cnt++;
                            notifyDataSetChanged();
                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


//        from user->posts
        ref.child("users").child(auth.getUid()).child("posts").child(post.getPost_id()).removeValue();

//        from posts
        ref.child("posts").child(post.getPost_id()).removeValue();

//        from likes
        ref.child("likes").child(post.getPost_id()).removeValue();

//        from saved
        ref.child("saved").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot _user_ids : snapshot.getChildren()) {
                    _user_ids.child(post.getPost_id()).getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        from comments
        ref.child("comments").child(post.getPost_id()).removeValue();

        long saved_count = mContext.getSharedPreferences("shared_pref", Context.MODE_PRIVATE)
                .getLong("notification_count", 0);
        mContext.getSharedPreferences("shared_pref", Context.MODE_PRIVATE).edit()
                .putLong("notification_count", max(saved_count - cnt, 0)).apply();

        Toast.makeText(mContext, "Deleted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView postImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            postImage = itemView.findViewById(R.id.post_image);
        }
    }

}
