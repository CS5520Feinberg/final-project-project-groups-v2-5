package edu.northeastern.rhythmlounge;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    private Button logoutButton, deleteAccountButton;

    /**
     * Initializes the UI and Firebase instance when the activity is created.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initializes firebase auth
        mAuth = FirebaseAuth.getInstance();

        // Map ui elements
        logoutButton = findViewById(R.id.buttonLogoutInSettings);
        deleteAccountButton = findViewById(R.id.buttonDeleteAccountInSettings);

        // Set logout on click
        logoutButton.setOnClickListener(v -> logout());

        // Set deletion dialog on click
        deleteAccountButton.setOnClickListener(v -> showReauthDialog());

    }

    /**
     * Shows a dialog to confirm account deletion and proceeds to ReAutheActivity.
     */
    private void showReauthDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle("Confirm Deletion");
        builder.setMessage("Deleting this account will permanently remove all associated data. This action cannot be undone. You must re-authenticate before you can delete your account. Proceed to re-authentication?");

        // If the user proceeds, navigate to reauthentication screen and eventual deletion.
        builder.setPositiveButton("Proceed", (dialog, which) -> {

            Intent reauthIntent = new Intent(SettingsActivity.this, ReAuthActivity.class);
            startActivity(reauthIntent);
        });

        // If the user chooses to cancel, dismiss the dialog.
        builder.setNegativeButton("Cancel", ((dialog, which) -> dialog.dismiss()));

        AlertDialog alert = builder.create();
        alert.show();
    }


    /**
     * Helper method to log the user out.
     * This was taken out of self-user page and fit into the settings activity. 
     */
    private void logout() {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("YOUR_REQUEST_ID_TOKEN")
                    .requestEmail()
                    .build();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

            googleSignInClient.signOut().addOnCompleteListener(this,
                    task -> {
                        mAuth.signOut();

                        Intent intent = new Intent(this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        this.finish();

                        // Use getActivity() as the context for the Toast
                        Toast.makeText(this, "Logout successful.", Toast.LENGTH_SHORT).show();
                    });

    }
}
