package edu.northeastern.rhythmlounge;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileFragment extends Fragment {

    private EditText editTextUsername, editTextEmail;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        editTextUsername = rootView.findViewById(R.id.editTextUsername);
        editTextEmail = rootView.findViewById(R.id.editTextEmail);
        ImageView imageViewProfilePic = rootView.findViewById(R.id.edit_profile_pic);
        Button buttonSaveChanges = rootView.findViewById(R.id.buttonSaveChanges);

        String currentUserId = getCurrentUserId();
        retrieveCurrentUser(currentUserId);

        buttonSaveChanges.setOnClickListener(v -> saveChanges());

        Button buttonEdit = rootView.findViewById(R.id.buttonEdit);


        buttonSaveChanges.setOnClickListener(v -> saveChanges());

        buttonEdit.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        return rootView;
    }

    private void retrieveCurrentUser(String userId) {
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User currentUser = documentSnapshot.toObject(User.class);
                if (currentUser != null) {
                    editTextUsername.setText(currentUser.getUsername());
                    editTextEmail.setText(currentUser.getEmail());
                }
            } else {
                Log.d("EditProfileFragment", "Document does not exist for user: " + userId);
            }
        }).addOnFailureListener(e -> Log.e("EditProfileFragment", "Error retrieving user data: " + e.getMessage()));
    }


    private void saveChanges() {
        String newUsername = editTextUsername.getText().toString();
        String newEmail = editTextEmail.getText().toString();

        String currentUserId = getCurrentUserId();
        assert currentUserId != null;
        DocumentReference userRef = db.collection("users").document(currentUserId);

        userRef.update("username", newUsername, "email", newEmail)
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) {
                        UserViewModel userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
                        userViewModel.setUsername(newUsername);
                        userViewModel.setEmail(newEmail);
                    }

                    Toast.makeText(requireContext(), "Changes saved successfully!", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to save changes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("EditProfileFragment", "Error updating user data: " + e.getMessage());
                });
    }

    private String getCurrentUserId() {
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }
        return null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }
}


