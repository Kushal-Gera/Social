package kushal.application.social.Fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kushal.application.social.Adapters.NotificationAdapter;
import kushal.application.social.Models.Notification;
import kushal.application.social.R;

public class DiscoverFrag extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;
    FirebaseUser auth;
    LottieAnimationView animationView;
    TextView tv;
    private static String SHARED_PREF = "shared_pref";

    ShimmerFrameLayout shimmer;
    ImageView close;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        auth = FirebaseAuth.getInstance().getCurrentUser();

        close = view.findViewById(R.id.close);
        animationView = view.findViewById(R.id.animationView);
        tv = view.findViewById(R.id.tv);

        shimmer = view.findViewById(R.id.shimmer_view_discover);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(getContext(), notificationList);
        recyclerView.setAdapter(notificationAdapter);

        close.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        new MyTask().execute();

        return view;
    }

    public class MyTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {

//            readNotifications();
            FirebaseDatabase.getInstance().getReference().child("notifications")
                    .child(auth.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    notificationList.clear();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        notificationList.add(snapshot.getValue(Notification.class));
                    }

                    Collections.reverse(notificationList);
                    notificationAdapter.notifyDataSetChanged();
                    shimmer.setVisibility(View.GONE);

                    if (notificationList.isEmpty()){
                        animationView.setVisibility(View.VISIBLE);
                        tv.setVisibility(View.VISIBLE);
                    }
                    else{
                        animationView.setVisibility(View.GONE);
                        tv.setVisibility(View.GONE);
                    }

                    long notification_count = notificationList.size();
                    if (getActivity() != null)
                        getActivity().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
                                .edit().putLong("notification_count", notification_count).apply();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

            return null;
        }
    }

}