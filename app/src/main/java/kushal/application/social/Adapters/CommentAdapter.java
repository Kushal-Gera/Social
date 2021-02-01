package kushal.application.social.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kushal.application.social.Models.Comment;
import kushal.application.social.ProfileActivity;
import kushal.application.social.R;


public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private Context mContext;
    private List<Comment> mComments;

    String post_id;

    private FirebaseUser auth;

    public CommentAdapter(Context mContext, List<Comment> mComments, String post_id) {
        this.mContext = mContext;
        this.mComments = mComments;
        this.post_id = post_id;
        auth = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false);
        return new CommentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Comment comment = mComments.get(position);

        holder.comment.setText(comment.getComment());

        if (TextUtils.isEmpty(comment.getImage_url()))
            holder.imageProfile.setImageResource(R.drawable.ic_baseline_person);
        else Picasso.get().load(comment.getImage_url()).into(holder.imageProfile);

        FirebaseDatabase.getInstance().getReference().child("users")
                .child(comment.getUser_id()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.user_name.setText(dataSnapshot.child("user_name").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (comment.getUser_id().endsWith(auth.getUid())) {
                AlertDialog alertDialog =
                        new AlertDialog.Builder(mContext, R.style.progress_dialog_theme).create();
                alertDialog.setTitle("Do you want to delete it ?");
                alertDialog.setMessage("It can never be restored.");

                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", (dialog, which) -> dialog.dismiss());
                alertDialog.
                        setButton(AlertDialog.BUTTON_POSITIVE, "Yes", (dialog, which) ->
                                FirebaseDatabase.getInstance()
                                        .getReference().child("comments")
                                        .child(post_id).child(comment.getId()).removeValue()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                dialog.dismiss();
                                            }
                                        })
                        );

                alertDialog.show();
            }

            return true;
        });

        holder.user_name.setOnClickListener(v -> {
            Intent i = new Intent(mContext, ProfileActivity.class);
            i.putExtra("user_id", comment.getUser_id());
            mContext.startActivity(i);
        });

    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView imageProfile;
        public TextView user_name;
        public TextView comment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageProfile = itemView.findViewById(R.id.image_profile);
            user_name = itemView.findViewById(R.id.user_name);
            comment = itemView.findViewById(R.id.comment);
        }
    }

}