package kushal.application.social.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import kushal.application.social.Adapters.PostAdapter;
import kushal.application.social.Models.Post;
import kushal.application.social.R;

public class HomeFrag extends Fragment {

    ArrayList<Post> mlist;
    FirebaseUser auth;
    RecyclerView recycler_view;
    PostAdapter adapter;
    ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        auth = FirebaseAuth.getInstance().getCurrentUser();

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

        loadData();
        return view;
    }

    void loadData() {
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}