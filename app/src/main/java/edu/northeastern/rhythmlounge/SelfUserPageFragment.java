package edu.northeastern.rhythmlounge;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class SelfUserPageFragment extends Fragment {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private TextView textViewOwnUsername, textViewOwnEmail, textViewOwnFollowers, textViewOwnFollowing;
    private ImageView imageViewProfilePic;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageReference;

    private ActivityResultLauncher<String> pickMedia;

    private Button buttonLogout;

    public SelfUserPageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_self_user_page, container, false);

        initializeViewElements(view);
        initializeFirebaseElements();

        String currentUserId = getCurrentUserId();
        retrieveCurrentUser(currentUserId);

        checkPermission();
        textViewOwnFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFollowersClicked();
            }
        });

        // Set click listener for "Following" TextView
        textViewOwnFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFollowingClicked();
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken("YOUR_REQUEST_ID_TOKEN")
                        .requestEmail()
                        .build();
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

                // Google sign out
                googleSignInClient.signOut().addOnCompleteListener(getActivity(),
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                // Firebase sign out
                                mAuth.signOut();

                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                requireActivity().finish();

                                // Use getActivity() as the context for the Toast
                                Toast.makeText(getActivity(), "Logout successful.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });



        pickMedia = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: " + uri);

                Glide.with(this).load(uri).into(imageViewProfilePic);
                uploadImageToFirebaseStorage(uri);
            } else {
                Log.d("PhotoPicker", "No media selected");
            }
        });

        imageViewProfilePic.setOnClickListener(v -> openImageDialog());

        return view;
    }

    public void onFollowersClicked() {
        // Implement the behavior when "Followers" is clicked
        Intent intent = new Intent(getActivity(), FollowersActivity.class);
        intent.putExtra("USER_ID", getCurrentUserId());
        startActivity(intent);
    }

    public void onFollowingClicked() {
        // Implement the behavior when "Following" is clicked
        Intent intent = new Intent(getActivity(), FollowingActivity.class);
        intent.putExtra("USER_ID", getCurrentUserId());
        startActivity(intent);
    }

    private void initializeViewElements(View view) {
        textViewOwnUsername = view.findViewById(R.id.textViewOwnUsername);
        textViewOwnEmail = view.findViewById(R.id.textViewOwnEmail);
        textViewOwnFollowers = view.findViewById(R.id.textViewOwnFollowers);
        textViewOwnFollowing = view.findViewById(R.id.textViewOwnFollowing);
        imageViewProfilePic = view.findViewById(R.id.profile_pic);
        buttonLogout = view.findViewById(R.id.button_logout);
    }

    private void initializeFirebaseElements() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseStorage fbStorage = FirebaseStorage.getInstance();
        storageReference = fbStorage.getReference();
    }

    private String getCurrentUserId() {
        return Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
    }

    private void retrieveCurrentUser(String userId) {
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            User currentUser = documentSnapshot.toObject(User.class);
            if (currentUser != null) {
                populateUIWithCurrentUserDetails(currentUser);
            } else {
                Log.w("FirebaseError", "No user found with id: " + userId);
            }
        });
    }

    private void populateUIWithCurrentUserDetails(User currentUser) {
        textViewOwnUsername.setText(currentUser.getUsername());
        textViewOwnEmail.setText(currentUser.getEmail());

        int followerCount = (currentUser.getFollowers() != null) ? currentUser.getFollowers().size() : 0;
        int followingCount = (currentUser.getFollowing() != null) ? currentUser.getFollowing().size() : 0;

        String followingText = followingCount + " Following • " + followerCount + " Followers";

        textViewOwnFollowers.setText(followingText);
        textViewOwnFollowing.setText("");

        if (currentUser.getProfilePictureUrl() != null && !currentUser.getProfilePictureUrl().isEmpty()) {
            // Load the image from the URL if it exists
            Glide.with(this).load(currentUser.getProfilePictureUrl()).into(imageViewProfilePic);
        } else {
            // Load a default image if the user has not set their profile picture
            Glide.with(this).load(R.drawable.defaultprofilepicture).into(imageViewProfilePic);
        }
    }


    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(requireActivity())
                        .setTitle("Permission needed")
                        .setMessage("This permission is needed to access your media.")
                        .setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE))
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .create().show();
            } else {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void openImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Update Profile Picture")
                .setMessage("Would you like to upload a new profile picture?")
                .setPositiveButton("Yes", (dialog, which) -> pickMedia.launch("image/*"))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void uploadImageToFirebaseStorage(Uri imageUri) {
        String userId = getCurrentUserId();
        StorageReference profilePicRef = storageReference.child("profile_pics/" + userId);

        profilePicRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> profilePicRef.getDownloadUrl().addOnSuccessListener(uri -> {
            String imageUrl = uri.toString();

            db.collection("users").document(userId).update("profilePictureUrl", imageUrl).addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "Profile picture updated", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                Log.d("FirebaseError", "Firebase update failure: " + e.getMessage());
            });
        })).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Failed to upload profile picture", Toast.LENGTH_SHORT).show();
            Log.d("FirebaseError", "Firebase Storage upload failure: " + e.getMessage());
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}




