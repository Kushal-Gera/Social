package kushal.application.social;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {
    private static String VERIFICATION_ID;

    FirebaseAuth auth;

    TextInputLayout phone, otp;
    TextView login, next;
    ProgressBar progressBar;
    LinearLayout linearLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        phone = findViewById(R.id.phone);
        otp = findViewById(R.id.otp);

        next = findViewById(R.id.next);
        login = findViewById(R.id.login_btn);
        linearLayout = findViewById(R.id.linearLayout);
        progressBar = findViewById(R.id.progressBar);


        next.setOnClickListener(v -> {
            String number = phone.getEditText().getText().toString().trim();

            if (!TextUtils.isEmpty(number) && number.length() > 9) {

                getVerificationCode('+' + "91" + number);

                progressBar.setVisibility(View.VISIBLE);
                otp.setVisibility(View.VISIBLE);
                login.setVisibility(View.VISIBLE);
                next.setVisibility(View.INVISIBLE);
//                return;
            } else {
                phone.getEditText().setError("Phone Number Required");
                phone.requestFocus();
            }
//
//              even I don't how this coding is working, I just pasted it :p
//            View view = getCurrentFocus();
//            if (view != null) {
//                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//            }
//              but I am sure that after this 'keyboard is gone'
        });

        login.setOnClickListener(v -> {
            String userCode = otp.getEditText().getText().toString().trim();

            if (!TextUtils.isEmpty(userCode))
                verifyCode(userCode);
            else {
                otp.getEditText().setError("OTP Required");
                otp.requestFocus();
            }
        });

    }

    private void verifyCode(String userCode) {
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(VERIFICATION_ID, userCode);
            signInWith(credential);
        } catch (Exception e) {
            Toast.makeText(this, "Invalid OTP", Toast.LENGTH_LONG).show();
//            startActivity(new Intent(this, LoginActivity.class));
//            finish();
        }
    }

    private void signInWith(PhoneAuthCredential credential) {
        Toast.makeText(this, "Hold on, Just There...", Toast.LENGTH_SHORT).show();

        auth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(LoginActivity.this, "Error Occurred", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, LoginActivity.class));
            finish();
        });

    }

    private void getVerificationCode(String phoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder()
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            Log.d("onVerificationCompleted", "onVerificationCompleted:" + credential);
            signInWith(credential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Log.e("onVerificationFailed", "onVerificationFailed", e);
        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
            Log.d("onCodeSent", "onCodeSent:" + verificationId);

            VERIFICATION_ID = verificationId;
        }
    };

//    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
//            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//                @Override
//                public void onVerificationCompleted(PhoneAuthCredential credential) {
//                    String userCode = credential.getSmsCode();
//                    if (userCode != null) {
//                        otp.getEditText().setText(userCode);
//                        verifyCode(userCode);
//                    }
//                }
//
//                @Override
//                public void onVerificationFailed(FirebaseException e) {
//                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
//                }
//
//                @Override
//                public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
//                    super.onCodeSent(s, forceResendingToken);
//                    VERIFICATION_ID = s;
//                }
//            };

}