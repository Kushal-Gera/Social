package kushal.application.social;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import kushal.application.social.Fragments.DiscoverFrag;
import kushal.application.social.Fragments.HomeFrag;
import kushal.application.social.Fragments.ProfileFrag;
import kushal.application.social.Fragments.SearchFrag;

public class MainActivity extends AppCompatActivity {

    FrameLayout container;
    BottomNavigationView bottom_navbar;
    DatabaseReference ref;
    FirebaseUser auth;

    ImageView post_now;
    FrameLayout chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        container = findViewById(R.id.container);
        bottom_navbar = findViewById(R.id.bottom_navbar);

        post_now = findViewById(R.id.post_now);
        chat = findViewById(R.id.chat);

        auth = FirebaseAuth.getInstance().getCurrentUser();
        ref = FirebaseDatabase.getInstance().getReference();
        if (auth == null) {
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        }

        chat.setOnLongClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            return true;
        });

        Query q = ref.child("users").child(auth.getUid());
        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.child("name").exists()) {
//                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                    Toast.makeText(getApplicationContext(), "Name not Found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        setBottomNavBar();
        getSupportFragmentManager().beginTransaction().replace(container.getId(), new HomeFrag()).commit();

        post_now.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, PostActivity.class));
        });
    }

    @SuppressLint("NonConstantResourceId")
    private void setBottomNavBar() {

        bottom_navbar.setOnNavigationItemSelectedListener(item -> {
            Fragment frag = null;

            switch (item.getItemId()) {
                case R.id.home:
                    frag = new HomeFrag();
                    break;
                case R.id.search:
                    frag = new SearchFrag();
                    break;
                case R.id.post:
                    startActivity(new Intent(MainActivity.this, PostActivity.class));
                    break;
                case R.id.discover:
                    frag = new DiscoverFrag();
                    break;
                case R.id.profile:
                    frag = new ProfileFrag();
                    break;
                default:
                    break;
            }

            if (frag != null)
                getSupportFragmentManager().beginTransaction().replace(container.getId(), frag).commit();

            return true;
        });

    }
}