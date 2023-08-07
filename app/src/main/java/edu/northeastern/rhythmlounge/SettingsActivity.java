package edu.northeastern.rhythmlounge;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

/**
 * SettingsActivity handles user account settings functionalities, including email and password changes, account deletion, and logging out.
 */
public class SettingsActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    /**
     * Initializes the UI and Firebase instance when the activity is created.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initializes firebase auth
        mAuth = FirebaseAuth.getInstance();

        // Map ui elements
        Button logoutButton = findViewById(R.id.buttonLogoutInSettings);
        Button changeEmailButton = findViewById(R.id.buttonChangeEmailInSettings);
        Button changePasswordButton = findViewById(R.id.buttonChangePasswordInSettings);
        Button deleteAccountButton = findViewById(R.id.buttonDeleteAccountInSettings);

        // Set up listeners for the button clicks
        logoutButton.setOnClickListener(v -> logout());
        changeEmailButton.setOnClickListener(v -> changeEmail());
        changePasswordButton.setOnClickListener(v -> changePassword());
        deleteAccountButton.setOnClickListener(v -> showReauthDialog());

    }

    /**
     * Presents a dialog to a the user confirming account deletion.
     * If confirmed, user proceeds to ReAuthActivity for re-authentication.
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
     * Handles the logout process, signing the user out then navigating back to the main activity.
     */
    private void logout() {

        // Configure Google Sign-In to request the user's email address.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("YOUR_REQUEST_ID_TOKEN")
                .requestEmail()
                .build();

        // Build a Google SignInclient with the options specified by google.
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInClient.signOut().addOnCompleteListener(this,
                task -> {
                    // Sign out of Firebase
                    mAuth.signOut();

                    // Create intent to navigate to the main activity
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    // Finish current activity
                    this.finish();

                    // Display a toast message confirming successful logout.
                    Toast.makeText(this, "Logout successful.", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Shows a dialog allowing users to change their registered email.
     * Users must provide their new desired email and current password.
     */
    private void changeEmail() {

        // Create a new vertical LinearLayout for the dialog
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 0, 50, 0);

        // Create an input field for the new email
        EditText input1 = new EditText(this);
        input1.setHint("New Email");
        input1.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        layout.addView(input1);

        // Create another input field to confirm the new email
        EditText input2 = new EditText(this);
        input2.setHint("Confirm New Email");
        input2.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        layout.addView(input2);

        // Create an input field for the current password
        EditText passwordInput = new EditText(this);
        passwordInput.setHint("Current Password");
        passwordInput.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(passwordInput);

        // Configure the AlertDialog to show the input fields and button
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle("Change Email")
               .setView(layout)
               .setPositiveButton("Change", null)
               .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        // Add a listener to handle the positive button's click event.
        dialog.setOnShowListener(d -> {
            Button positiveButton = ((AlertDialog) d).getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                // Extract user input from the fields
                String newEmail1 = input1.getText().toString().trim();
                String newEmail2 = input2.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                // Validate inputs
                if (TextUtils.isEmpty(newEmail1) || TextUtils.isEmpty(newEmail2) || TextUtils.isEmpty(password)) {
                    Toast.makeText(SettingsActivity.this, "Fill out all the fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newEmail1.equals(newEmail2)) {
                    Toast.makeText(SettingsActivity.this, "Emails don't match!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // If valid, proceed to reauthenticate and change the email
                reauthenticateAndChangeEmail(newEmail1, password, dialog);
            });
        });
        dialog.show();
    }

    /**
     * Re-authenticats the user and changes their email address if successful.
     * @param newEmail New email address to set.
     * @param password Current password for re-authentication.
     * @param dialog   The current active dialog for email change.
     */
    private void reauthenticateAndChangeEmail(String newEmail, String password, AlertDialog dialog) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(user.getEmail()), password);

        // Reauthenticate the user with the provided credential.
        user.reauthenticate(credential)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Once reauthenticated, update the user's email
                    user.updateEmail(newEmail)
                        .addOnCompleteListener(emailUpdateTask -> {
                            if (emailUpdateTask.isSuccessful()) {
                               // If email update is successful, also update it in Firestore database
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("users").document(user.getUid())
                                    .update("email", newEmail)
                                    .addOnSuccessListener(void1 -> {
                                        Toast.makeText(SettingsActivity.this, "Email changed successfully.", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(SettingsActivity.this, "Failed to update email in the database.", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    });
                            } else {
                                Toast.makeText(SettingsActivity.this, "Failed to update email.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });
                } else {
                    Toast.makeText(SettingsActivity.this, "Re-authentication failed.", Toast.LENGTH_SHORT).show();
                }
            });
    }

    /**
     * Displays a dialog allowing users to change their password.
     * Users must provide their current email, current password, and new desired password
     */
    private void changePassword() {

        // Creates the layout for the password change dialog
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 0, 50, 0);

        // Create EditText fields for current email,
        EditText emailInput = new EditText(this);
        emailInput.setHint("Current Email");
        emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        layout.addView(emailInput);

        // Current password
        EditText currentPasswordInput = new EditText(this);
        currentPasswordInput.setHint("Current Password");
        currentPasswordInput.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(currentPasswordInput);

        // Current new password
        EditText newPasswordInput = new EditText(this);
        newPasswordInput.setHint("New Password");
        newPasswordInput.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(newPasswordInput);

        // Confirm new password
        EditText confirmNewPasswordInput = new EditText(this);
        confirmNewPasswordInput.setHint("Confirm New Password");
        confirmNewPasswordInput.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(confirmNewPasswordInput);

        // Create and configure the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle("Change Password")
                .setView(layout)
                .setPositiveButton("Change", null)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        // Set action for the positive button of the dialog
        dialog.setOnShowListener(d -> {
            Button positiveButton = ((AlertDialog) d).getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(e -> {
                // Extract user input
                String email = emailInput.getText().toString().trim();
                String currentPassword = currentPasswordInput.getText().toString().trim();
                String newPassword = newPasswordInput.getText().toString().trim();
                String confirmNewPassword = confirmNewPasswordInput.getText().toString().trim();

                // Validate the user input
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmNewPassword)) {
                Toast.makeText(SettingsActivity.this, "Fill out all the fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmNewPassword)) {
                Toast.makeText(SettingsActivity.this, "Passwords don't match!", Toast.LENGTH_SHORT).show();
                return;
            }
                // Proceed to reauthenticate and change password
                reauthenticateAndChangePassword(email, currentPassword, newPassword, dialog);
            });
        });
        dialog.show();
    }

    /**
     * Re-authenticates the user and changes their password if successful.
     * Users
     * @param email           User's current email address
     * @param currentPassword Current password for reauthentication
     * @param newPassword     New password to set
     * @param dialog          The current active dialog for password change
     */
    private void reauthenticateAndChangePassword(String email, String currentPassword, String newPassword, AlertDialog dialog) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);
        // User reauthentication, this is needed to move forward with the password change.
        user.reauthenticate(credential)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user.updatePassword(newPassword)
                        .addOnCompleteListener(passwordUpdateTask -> {
                            if (passwordUpdateTask.isSuccessful()) {
                                Toast.makeText(SettingsActivity.this, "Password changed successfully.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(SettingsActivity.this, "Failed to update password.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });
                } else {
                    Toast.makeText(SettingsActivity.this, "Re-authentication failed.", Toast.LENGTH_SHORT).show();
                }
            });
    }
}
