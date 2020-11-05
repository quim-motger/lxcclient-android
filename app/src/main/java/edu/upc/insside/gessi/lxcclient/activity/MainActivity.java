package edu.upc.insside.gessi.lxcclient.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import edu.upc.insside.gessi.lxcclient.R;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    private TextView welcomeMessage;
    private SignInButton signInGoogleButton;
    private Button joinButton;
    private Button logOutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Wire layout items to Java objects
        populateViewItems();

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.default_web_client_id))
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();

        // Set buttons on click listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.log_out_button).setOnClickListener(this);
        findViewById(R.id.join_cluster_button).setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.log_out_button:
                logOut();
                break;
            case R.id.join_cluster_button:
                joinCluster();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }

    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        updateUI(null);
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void logOut() {
        mAuth.signOut();
        updateUI(null);
    }

    private void joinCluster() {
        Snackbar.make(findViewById(android.R.id.content), "You will join a cluster... one day!", Snackbar.LENGTH_LONG).show();
    }

    private void updateUI(FirebaseUser user) {
        String message;
        if (user == null) {
            message = "Hey, you! Welcome!\nI don't know who you are...";
            updateButtons(false);
        } else {
            message = String.format("Hey, welcome %s!", user.getDisplayName());
            updateButtons(true);
        }
        welcomeMessage.setText(message);
    }

    private void updateButtons(boolean isLoggedIn) {
        signInGoogleButton.setVisibility(isLoggedIn ? View.INVISIBLE : View.VISIBLE);
        logOutButton.setVisibility(isLoggedIn ? View.VISIBLE : View.INVISIBLE);
        joinButton.setVisibility(isLoggedIn ? View.VISIBLE : View.INVISIBLE);
    }

    private void populateViewItems() {
        welcomeMessage = findViewById(R.id.welcome_message);
        signInGoogleButton = findViewById(R.id.sign_in_button);
        logOutButton = findViewById(R.id.log_out_button);
        joinButton = findViewById(R.id.join_cluster_button);
    }

}