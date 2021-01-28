package kushal.application.social.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kushal.application.social.Models.User;
import kushal.application.social.R;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private final Context mContext;
    private final List<User> mUsers;
    private final boolean isFargment;

    private FirebaseUser auth;

    public SearchAdapter(Context mContext, List<User> mUsers, boolean isFargment) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isFargment = isFargment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new SearchAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        auth = FirebaseAuth.getInstance().getCurrentUser();

        final User user = mUsers.get(position);
        holder.btnFollow.setVisibility(View.VISIBLE);

        holder.name.setText(user.getName());

        String url = "https://firebasestorage.googleapis.com/v0/b/social-1ab90.appspot.com/o/posts%2F1611854297138.null?alt=media&token=4c47fa06-4dcb-4149-8ee2-44d15a941f96";
        if (!user.getImage_url().isEmpty())
            Picasso.get().load(user.getImage_url())
                    .placeholder(R.drawable.person_fill).into(holder.imageProfile);

        isFollowed(user.getId(), holder.btnFollow);

        if (user.getId().equals(auth.getUid())) {
            holder.btnFollow.setVisibility(View.GONE);
        }

        holder.btnFollow.setOnClickListener(v -> {

            if (holder.btnFollow.getText().toString().equals("Follow")) {

                FirebaseDatabase.getInstance().getReference().child("users").child(auth.getUid())
                        .child("following").child(user.getId()).setValue(true);

                FirebaseDatabase.getInstance().getReference().child("users").child(user.getId())
                        .child("followers").child(auth.getUid()).setValue(true);

            } else {
                FirebaseDatabase.getInstance().getReference().child("users").child(auth.getUid())
                        .child("following").child(user.getId()).removeValue();

                FirebaseDatabase.getInstance().getReference().child("users").child(user.getId())
                        .child("followers").child(auth.getUid()).removeValue();
            }
        });

//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isFargment) {
//                    mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit().putString("profileId", user.getId()).apply();
//
////                    ((FragmentActivity) mContext)
////                            .getSupportFragmentManager().beginTransaction()
////                            .replace(R.id.con, new ProfileFragment()).commit();
//                }
//                else {
//                    Intent intent = new Intent(mContext, MainActivity.class);
//                    intent.putExtra("publisherId", user.getId());
//                    mContext.startActivity(intent);
//                }
//            }
//        });

    }

    private void isFollowed(final String id, final TextView btnFollow) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("users").child(auth.getUid()).child("following");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(id).exists()) {
                    btnFollow.setText("Following");
                    btnFollow.setTextColor(mContext.getResources().getColor(R.color.black));
                    btnFollow.setBackground(mContext.getResources().getDrawable(R.drawable.gray_btn_bg));
                } else {
                    btnFollow.setText("Follow");
                    btnFollow.setTextColor(mContext.getResources().getColor(R.color.white));
                    btnFollow.setBackground(mContext.getResources().getDrawable(R.drawable.blue_button_bg));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView imageProfile;
        public TextView name, btnFollow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageProfile = itemView.findViewById(R.id.image_profile);
            name = itemView.findViewById(R.id.name);
            btnFollow = itemView.findViewById(R.id.btn_follow);
        }
    }


}