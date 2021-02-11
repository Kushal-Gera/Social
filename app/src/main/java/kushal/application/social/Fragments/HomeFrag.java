package kushal.application.social.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import kushal.application.social.Adapters.PostAdapter;
import kushal.application.social.ChatActivity;
import kushal.application.social.Models.Post;
import kushal.application.social.PostActivity;
import kushal.application.social.R;

public class HomeFrag extends Fragment {

    ArrayList<Post> mlist;
    FirebaseUser auth;
    RecyclerView recycler_view;
    PostAdapter adapter;
    ProgressBar progressBar;

    ImageView post_now;
    FrameLayout chat;

    private static final String SHARED_PREF = "shared_pref";
    SharedPreferences pref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        auth = FirebaseAuth.getInstance().getCurrentUser();

        post_now = view.findViewById(R.id.post_now);
        chat = view.findViewById(R.id.chat);

        progressBar = view.findViewById(R.id.progressBar);
        recycler_view = view.findViewById(R.id.recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);
        recycler_view.setLayoutManager(llm);

        mlist = new ArrayList<>();
        progressBar.setVisibility(View.VISIBLE);

        adapter = new PostAdapter(getContext(), mlist);
        recycler_view.setAdapter(adapter);
        pref = getContext().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);


        post_now.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), PostActivity.class));
        });

        chat.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), ChatActivity.class));
        });


        MyTask myTask = new MyTask();
        myTask.execute();


        return view;
    }

    public class MyTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {
            Query query = FirebaseDatabase.getInstance().getReference().child("posts");

            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mlist.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Post post = dataSnapshot.getValue(Post.class);
                        mlist.add(post);
                    }

                    progressBar.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();

                    if (!mlist.isEmpty()) {
                        int times = pref.getInt("first_home", 0);
                        if (times < 3) {
                            Snackbar.make(getActivity().findViewById(R.id.container),
                                    "Long press on posts to change scale-type",
                                    Snackbar.LENGTH_LONG).show();
                            pref.edit().putInt("first_home", times + 1).apply();
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            return null;
        }
    }

}