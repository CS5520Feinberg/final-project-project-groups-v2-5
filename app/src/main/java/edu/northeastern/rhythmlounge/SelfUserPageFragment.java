package edu.northeastern.rhythmlounge;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.northeastern.rhythmlounge.Playlists.SelfUserPlaylistAdapter;

public class SelfUserPageFragment extends Fragment {

    private static final String TAG = "SelfUserPageFragment";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int ERROR_DIALOGUE_REQ = 9001;
    private TextView textViewOwnUsername, textViewOwnBio, textViewOwnFollowers, textViewOwnFollowing;
    private ImageView imageViewProfilePic, heatMap;

    private RecyclerView recyclerView;
    private SelfUserPlaylistAdapter selfUserPlaylistAdapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private ActivityResultLauncher<String> pickMedia;

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

        textViewOwnFollowers.setOnClickListener(v -> onFollowersClicked());
        textViewOwnFollowing.setOnClickListener(v -> onFollowingClicked());

        Button buttonEdit = view.findViewById(R.id.button_edit);
        buttonEdit.setOnClickListener(v -> showEditProfileDialog());

        Button buttonAddPlaylist = view.findViewById(R.id.addPlaylistButton);
        buttonAddPlaylist.setOnClickListener(v -> addNewPlaylist());

        UserViewModel userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        userViewModel.getUsernameLiveData().observe(getViewLifecycleOwner(), username -> {
            if (username != null) {
                textViewOwnUsername.setText(username);
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

        initializePlaylistRecyclerView(view);

        imageViewProfilePic.setOnClickListener(v -> openImageDialog());

        heatMap.setOnClickListener(v -> openHeatMap());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        String currentUserId = getCurrentUserId();
        retrieveCurrentUser(currentUserId);
    }

    private void openHeatMap() {
        Intent intent = new Intent(getActivity(), HeatMapsActivity.class);
        intent.putExtra("USER_ID", getCurrentUserId());
        startActivity(intent);
    }

    public void onFollowersClicked() {
        Intent intent = new Intent(getActivity(), FollowersActivity.class);
        intent.putExtra("USER_ID", getCurrentUserId());
        startActivity(intent);
    }

    public void onFollowingClicked() {

        Intent intent = new Intent(getActivity(), FollowingActivity.class);
        intent.putExtra("USER_ID", getCurrentUserId());
        startActivity(intent);
    }

    private void initializeViewElements(View view) {
        textViewOwnUsername = view.findViewById(R.id.textViewOwnUsername);
        textViewOwnBio = view.findViewById(R.id.textViewOwnBio);
        textViewOwnFollowers = view.findViewById(R.id.textViewOwnFollowers);
        textViewOwnFollowing = view.findViewById(R.id.textViewOwnFollowing);
        imageViewProfilePic = view.findViewById(R.id.profile_pic);
        heatMap = view.findViewById(R.id.map_icon);

    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Edit Profile");

        View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        EditText editTextUsername = dialogLayout.findViewById(R.id.editTextUsername);
        EditText editTextBio = dialogLayout.findViewById(R.id.editTextBio);
        TextView textViewBioCounter = dialogLayout.findViewById(R.id.textViewBioCounter);

        editTextUsername.setText(textViewOwnUsername.getText());

        editTextBio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int remaining = 150 - s.toString().length();
                textViewBioCounter.setText("Remaining characters: " + remaining);
            }
        });

        builder.setView(dialogLayout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newUsername = editTextUsername.getText().toString();
            String newBio = editTextBio.getText().toString();
            updateProfileData(newUsername, newBio);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateProfileData(String newUsername, String newBio) {
        String currentUserId = getCurrentUserId();
        DocumentReference userRef = db.collection("users").document(currentUserId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", newUsername);
        updates.put("bio", newBio);

        userRef.update(updates)
                .addOnSuccessListener(e -> {
                    textViewOwnUsername.setText(newUsername);
                    textViewOwnBio.setText(newBio);
                    Toast.makeText(requireContext(), "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to update profile.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to update profile: " + e.getMessage());

                });
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

        if (currentUser.getBio() != null) {
            textViewOwnBio.setText(currentUser.getBio());
        }

        int followerCount = (currentUser.getFollowers() != null) ? currentUser.getFollowers().size() : 0;
        int followingCount = (currentUser.getFollowing() != null) ? currentUser.getFollowing().size() : 0;

        textViewOwnFollowers.setText("Followers: " + followerCount);
        textViewOwnFollowing.setText("Following: " + followingCount);

        if (currentUser.getProfilePictureUrl() != null && !currentUser.getProfilePictureUrl().isEmpty()) {
            Glide.with(this).load(currentUser.getProfilePictureUrl()).into(imageViewProfilePic);
        } else {
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


    private void initializePlaylistRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.playlisRecyclerView);
        selfUserPlaylistAdapter = new SelfUserPlaylistAdapter(getActivity(), new ArrayList<>());
        recyclerView.setAdapter(selfUserPlaylistAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        getCurrentUserPlaylists();
    }

     private void getCurrentUserPlaylists() {
        String currentUserId = getCurrentUserId();

        db.collection("users")
            .document(currentUserId)
            .collection("playlists")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> playlistSnapshots = task.getResult().getDocuments(); // Get snapshots
                    selfUserPlaylistAdapter.refreshData(playlistSnapshots);
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            });
    }

    private void addNewPlaylist() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Add Playlist");

        View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_add_playlist, null);
        EditText editTextPlaylistName = dialogLayout.findViewById(R.id.editTextPlaylistName);
        builder.setView(dialogLayout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String playlistName = editTextPlaylistName.getText().toString();
            if (!playlistName.isEmpty()) {
                savePlaylistToFirebase(playlistName);
            } else {
                Toast.makeText(requireContext(), "Playlist name cannot be blank.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void savePlaylistToFirebase(String playlistName) {
        String currentUserId = getCurrentUserId();
        Map<String, Object> playlist = new HashMap<>();
        playlist.put("name", playlistName);

        db.collection("users")
                .document(currentUserId)
                .collection("playlists")
                .add(playlist)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(requireContext(), "Playlist added successfully.", Toast.LENGTH_SHORT).show();
                    getCurrentUserPlaylists();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to add playlist.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to add playlist: " + e.getMessage());
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

    /**
     * Function to check if the device has appropriate Google Services Version
     * @return Boolean Value - True or False
     */
    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: Validating Google Services version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());
        /**
         * Everything is fine and User can make API calls
         */
        if (available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "isServicesOK: Google play services is working");
            return true;
        }
        /**
         * Error occurred but is resolvable
         */
        else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(TAG, "isServicesOK: Error occurred but is resolvable");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, ERROR_DIALOGUE_REQ);
            dialog.show();
        } else {
            Toast.makeText(getActivity(), "Error not resolvable", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}




