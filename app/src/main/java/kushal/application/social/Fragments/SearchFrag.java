package kushal.application.social.Fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

import kushal.application.social.Adapters.SearchAdapter;
import kushal.application.social.Adapters.TagAdapter;
import kushal.application.social.Models.User;
import kushal.application.social.R;

public class SearchFrag extends Fragment {
    private RecyclerView recyclerView;
    private List<User> mUsers;
    private SearchAdapter searchAdapter;

    private RecyclerView recyclerViewTags;
    private List<String> mHashTags;
    private TagAdapter tagAdapter;
    ProgressBar progress_bar;

    private SocialAutoCompleteTextView search_bar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        progress_bar = view.findViewById(R.id.progress_bar);
        progress_bar.setVisibility(View.VISIBLE);

        recyclerView = view.findViewById(R.id.recycler_view_users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerViewTags = view.findViewById(R.id.recycler_view_tags);
        recyclerViewTags.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewTags.setLayoutManager(llm);

        mHashTags = new ArrayList<>();
        tagAdapter = new TagAdapter(getContext(), mHashTags);
        recyclerViewTags.setAdapter(tagAdapter);

        mUsers = new ArrayList<>();
        searchAdapter = new SearchAdapter(getContext(), mUsers, true);
        recyclerView.setAdapter(searchAdapter);

        search_bar = view.findViewById(R.id.search_bar);

        MyTask myTask = new MyTask();
        myTask.execute();

        search_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUser(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        return view;
    }

    private void searchUser(String s) {

        Query query = FirebaseDatabase.getInstance().getReference().child("users")
                .orderByChild("name")
                .startAt(s.toUpperCase())
                .endAt(s.toLowerCase() + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    mUsers.add(user);
                }
                searchAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void filter(String text) {
        List<String> mSearchTags = new ArrayList<>();

        for (String s : mHashTags) {
            if (s.toLowerCase().contains(text.toLowerCase())) {
                mSearchTags.add(s);
            }
        }

        tagAdapter.filter(mSearchTags);
    }

    public class MyTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {

//            readTags();
            FirebaseDatabase.getInstance().getReference().child("hashtags")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mHashTags.clear();

                            for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                mHashTags.add(snapshot.getKey());

                            tagAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

//            readUsers();
            Query q = FirebaseDatabase.getInstance()
                    .getReference().child("users").orderByChild("name");

            q.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (TextUtils.isEmpty(search_bar.getText().toString())) {
                        mUsers.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            User user = snapshot.getValue(User.class);
                            mUsers.add(user);
                        }

                        searchAdapter.notifyDataSetChanged();
                        progress_bar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            return null;
        }
    }

}