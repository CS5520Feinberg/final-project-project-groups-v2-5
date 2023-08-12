package edu.northeastern.rhythmlounge.LoginRegisterAndAccount;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;
import java.util.Objects;

import edu.northeastern.rhythmlounge.HomeActivity;
import edu.northeastern.rhythmlounge.R;
import edu.northeastern.rhythmlounge.SelfUserPageFragment;

public class MainActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private static final int RC_SIGN_IN = 9001; // Request code for Google sign in
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("538248299353-25nkq6svfmd0qeepdar8udgs1kap8mrt.apps.googleusercontent.com") //Web client ID
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Add click listener to Google sign-in button
        findViewById(R.id.google_sign_in_button).setOnClickListener(view -> signInWithGoogle());



        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonGoToRegister = findViewById(R.id.buttonRegister);

        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            // check if the email and password fields are empty
            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(MainActivity.this, "Please fill out both fields.", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(MainActivity.this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                checkAndUpdateEmail(user);
                            }
                            // Sign in success, navigate to the lobby
                            Toast.makeText(MainActivity.this, "Authentication successful.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });


        buttonGoToRegister.setOnClickListener(v -> {
            // Start the Registration Activity
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // if the user is already signed in, then navigate to SelfUserPageActivity
        if (currentUser != null) {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        }
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
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
                firebaseAuthWithGoogle(account); // passing the entire GoogleSignInAccount
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(MainActivity.this, "Google sign in failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkAndUpdateEmail(user);
                        }
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    } else {
                        // If sign in fails, display a message to the user.
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            // This exception is thrown if a user tries to sign in with a Google account that has the same email
                            // as a user that was already created with another sign-in method.

                            // Ask the user if they want to link the accounts
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Account Linking")
                                    .setMessage("An account with this email address already exists. Do you want to link these accounts?")
                                    .setPositiveButton("Yes", (dialog, which) -> linkAccounts(credential))
                                    .setNegativeButton("No", null)
                                    .show();
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void linkAccounts(AuthCredential newCredential) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.linkWithCredential(newCredential)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "linkWithCredential:success");
                            startActivity(new Intent(MainActivity.this, SelfUserPageFragment.class));
                        } else {
                            Log.w(TAG, "linkWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Account linking failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void checkAndUpdateEmail(FirebaseUser user) {
        String authEmail = user.getEmail();
        db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> currentUser = documentSnapshot.getData();
            if (currentUser != null && currentUser.containsKey("email") && !Objects.equals(currentUser.get("email"), authEmail)) {
                documentSnapshot.getReference().update("email", authEmail)
                        .addOnSuccessListener(aVoid -> Log.d("MainActivity", "Email updated in Firestore"))
                        .addOnFailureListener(e -> Log.e("MainActivity", "Failed to update email in Firestore: " + e.getMessage()));
            }
        });
    }

}
