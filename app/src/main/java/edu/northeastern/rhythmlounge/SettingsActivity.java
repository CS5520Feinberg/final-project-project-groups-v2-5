package edu.northeastern.rhythmlounge;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    private Button deleteAccountButton;

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
        deleteAccountButton = findViewById(R.id.buttonDeleteAccount);

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
}
