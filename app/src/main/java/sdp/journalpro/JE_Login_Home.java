package sdp.journalpro;

//import android.app.ProgressDialog;

import android.content.Intent;
import android.support.annotation.NonNull;
//import android.support.annotation.VisibleForTesting;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
//import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.widget.Toast;

import static android.widget.Toast.makeText;

public class JE_Login_Home extends JE_Base_Activity implements View.OnClickListener, FirebaseAuth.AuthStateListener {

    private static final String TAG = "MyActivity";

    // Firebase variables
    private FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;

    // Text fields
    private EditText emailEditText;
    private EditText passwordEditText;

    // Buttons Layout
    Button loginBtn;
    Button forgotPasswordBtn;
    Button signupBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.je_login_home);

        // Form layout objects
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        // Firebase setup
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = this;

        //實作 loginButton 物件
        loginBtn = findViewById(R.id.login);

        //實作 forgetPasswordButton 物件
        forgotPasswordBtn = findViewById(R.id.forgetPassword);

        //實作 singupButton 物件
        signupBtn = findViewById(R.id.signup);

        //偵聽 Button 點擊事件
        loginBtn.setOnClickListener(this);

        //偵聽 Button 點擊事件
//        forgetPasswordBtn.setOnClickListener(this);
        //偵聽 Button 點擊事件
        signupBtn.setOnClickListener(this);
    }

    // Check login status
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        signInAlready(user);
//        mAuth.addAuthStateListener(mAuthListener);
        // Check if user is signed in (non-null) and update UI accordingly.
    }

    @Override
    public void onStop() {
        super.onStop();
//        mAuth.removeAuthStateListener(mAuthListener);
    }

    // If user status changes - verifies user
    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            Log.d(TAG, "user.isEmailVerified():" + user.isEmailVerified());
            Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
            signInAlready(user);
        } else {
            // User is signed out//
            Log.d(TAG, "onAuthStateChanged:signed_out");
            signOut();
        }
    }

    // Verify form data is valid
    private boolean validateForm() {
        boolean valid = true;

        // Email
        String email = emailEditText.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Required.");
            valid = false;
        } else {
            emailEditText.setError(null);
        }

        // Password
        String password = passwordEditText.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Required.");
            valid = false;
        } else {
            passwordEditText.setError(null);
        }

        return valid;
    }

    // Update ui with FireBase user data
    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            Intent intent = new Intent();
            intent.setClass(this, JE_Main_Home.class);
            startActivity(intent);
        } else {
            signupBtn.setVisibility(View.VISIBLE);
        }
    }

    // Verify if user already signed in
    private void signInAlready(FirebaseUser user) {
        if (user != null) {
            updateUI(user);
        } else {
            updateUI(null);
        }
    }

    // Signs the user out
    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

    // Create a user account through firebase, verification email
    private void createAccount(final String email, final String password) {
        if (!validateForm()) {
            return;
        }
        showProgressDialog();

        // [START create_user_with_email]
        Log.d(TAG, email);
        Log.d(TAG, password);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(JE_Login_Home.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            makeText(getApplicationContext(), R.string.toast_authentication_failed, Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        } else {
                            sendEmailVerification();
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]

                    }
                });
        // [END create_user_with_email]
    }

    // Sign in functionality, verifies account details with firebase
    private void signIn(String email, String password) {
        if (!validateForm()) {
            return;
        }
        showProgressDialog();
        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]

    }

    // Send email verification on user registration
    private void sendEmailVerification() {
        showProgressDialog();

        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        // Re-enable button
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(),
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(getApplicationContext(),
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END send_email_verification]
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        switch (i) {
            case R.id.login:
                // 點擊Btn後，要做的事情
                signIn(email, password);
                break;
//            case R.id.forgetPassword:
//                // 點擊Btn後，要做的事情
//                String test2 = "ForgetPassword";
//                Log.d(TAG, test2);
//                Log.d("1239999999", "456");
//                break;
            case R.id.signup:
                // 點擊Btn後，要做的事情
                createAccount(email, password);
                break;
            default:
                break;
        }
    }
}
