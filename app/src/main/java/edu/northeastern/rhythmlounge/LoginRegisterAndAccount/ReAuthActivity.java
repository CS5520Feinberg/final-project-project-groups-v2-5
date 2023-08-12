package edu.northeastern.rhythmlounge.LoginRegisterAndAccount;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import edu.northeastern.rhythmlounge.R;

/**
 * This activity handles user re-authentication, for the purpose of deleting a account in our Firebase database.
 */
public class ReAuthActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText editTextEmail, editTextPassword;

    /**
     * Sets up the UI and Firebase instances.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reauth);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Maps the UI elements for the activity.
        editTextEmail = findViewById(R.id.editTextEmailAuth);
        editTextPassword = findViewById(R.id.editTextPasswordAuth);
        Button reauthButton = findViewById(R.id.reauthButton);

        // Set a reauthentication on click.
        reauthButton.setOnClickListener(v -> reauthenticateUser());
    }

    /**
     * Reauthenticates the current user using the provided email and password.
     */
    private void reauthenticateUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validate inputs
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(ReAuthActivity.this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a credential with email and password
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Reauthenticate user
            user.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            //Toast.makeText(ReAuthActivity.this, "Reauthentication successful.", Toast.LENGTH_SHORT).show();
                            deleteUserAccount(); // call delete account if successful
                        } else {
                            Toast.makeText(ReAuthActivity.this, "Reauthentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    /**
     * Deletes the current user's account and moves user's data to "deleted_users" collection.
     * This also moves hosted events to "deleted_events" collection.
     */
    private void deleteUserAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userID = user.getUid();

            // Get references to all the Firestore collections
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userDoc = db.collection("users").document(userID);
            DocumentReference deletedUserDoc = db.collection("deleted_users").document(userID);
            CollectionReference eventsRef = db.collection("events");
            CollectionReference deletedEventsRef = db.collection("deleted_events");

            // Start new transaction to move user and event data.
            db.runTransaction((Transaction.Function<Void>) transaction -> {
                DocumentSnapshot userSnapshot = transaction.get(userDoc);
                if (userSnapshot.exists()) {
                    Map<String, Object> userData = Objects.requireNonNull(userSnapshot.getData());
                    // Get list of events the user is hosting.
                    List<String> hosting = (List<String>) userData.get("hosting");

                    // for each event the user is hosting, we need to move it to "deleted_events"
                    if (hosting != null) {
                        for (String eventID : hosting) {
                            DocumentReference eventDoc = eventsRef.document(eventID);
                            DocumentReference deletedEventDoc = deletedEventsRef.document(eventID);
                            DocumentSnapshot eventSnapshot = transaction.get(eventDoc);

                            if (eventSnapshot.exists()) {
                                Map<String, Object> eventData = Objects.requireNonNull(eventSnapshot.getData());
                                eventData.put("deleted_at", FieldValue.serverTimestamp());
                                transaction.set(deletedEventDoc, eventData);
                                transaction.delete(eventDoc);
                            }
                        }
                    }

                    userData.put("deleted_at", FieldValue.serverTimestamp());
                    transaction.set(deletedUserDoc, userData);
                    transaction.delete(userDoc);
                }
                return null;
            }).addOnSuccessListener(void1 -> {
                // Delete the user from firebase auth
                Log.d(TAG, "User and hosted events DocumentSnapshot successfully moved to deleted_users and deleted_events!");
                user.delete()
                        .addOnCompleteListener(e -> {
                            if (e.isSuccessful()) {
                                Toast.makeText(ReAuthActivity.this, "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                                // Send the user back to the mainactivity.
                                Intent intent = new Intent(ReAuthActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            } else {
                                Toast.makeText(ReAuthActivity.this, "Failed to delete account.", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Failed to delete user from Firebase Authentication", e.getException());
                            }
                        });
            }).addOnFailureListener(e -> Log.w(TAG, "Error moving user and hosted events DocumentSnapshot", e));
        }
    }
}
