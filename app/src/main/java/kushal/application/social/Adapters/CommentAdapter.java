package kushal.application.social.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kushal.application.social.Models.Comment;
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
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false);
        return new CommentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

//        auth = FirebaseAuth.getInstance().getCurrentUser();
//
//        final Comment comment = mComments.get(position);
//
//        holder.comment.setText(comment.getComment());
//
//        FirebaseDatabase.getInstance().getReference().child("Users").child(comment.getPublisher()).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                User user = dataSnapshot.getValue(User.class);
//
//                holder.user_name.setText(user.getUsername());
//                if (user.getImageurl().equals("default")) {
//                    holder.imageProfile.setImageResource(R.mipmap.ic_launcher);
//                } else {
//                    Picasso.get().load(user.getImageurl()).into(holder.imageProfile);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//
//        holder.comment.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(mContext, MainActivity.class);
//                intent.putExtra("publisherId", comment.getPublisher());
//                mContext.startActivity(intent);
//            }
//        });
//
//        holder.imageProfile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(mContext, MainActivity.class);
//                intent.putExtra("publisherId", comment.getPublisher());
//                mContext.startActivity(intent);
//            }
//        });
//
//        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                if (comment.getPublisher().endsWith(auth.getUid())) {
//                    AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
//                    alertDialog.setTitle("Do you want to delete?");
//                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "NO", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                        }
//                    });
//                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(final DialogInterface dialog, int which) {
//                            FirebaseDatabase.getInstance().getReference().child("Comments")
//                                    .child(post_id).child(comment.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//                                    if (task.isSuccessful()) {
//                                        Toast.makeText(mContext, "Comment deleted successfully!", Toast.LENGTH_SHORT).show();
//                                        dialog.dismiss();
//                                    }
//                                }
//                            });
//                        }
//                    });
//
//                    alertDialog.show();
//                }
//
//                return true;
//            }
//
//            ;
//        });

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