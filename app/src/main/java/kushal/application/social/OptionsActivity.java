package kushal.application.social;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class OptionsActivity extends AppCompatActivity {

    private TextView edit_profile;
    private TextView logOut;
    private ImageView close;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        edit_profile = findViewById(R.id.edit_profile);
        logOut = findViewById(R.id.logout);
        close = findViewById(R.id.close);

        logOut.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(this, R.style.progress_dialog_theme).create();
            dialog.setTitle("Do you want to Log Out ?");
            dialog.setMessage("You can login anytime.\nAll your data will be preserved.");

            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", (dialog1, which) -> dialog.dismiss());
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "yes", (dialog12, which) -> {
                FirebaseAuth.getInstance().signOut();

                Intent i = new Intent(OptionsActivity.this, LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            });
            dialog.show();
        });

        close.setOnClickListener(v -> finish());

        edit_profile.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

    }
}