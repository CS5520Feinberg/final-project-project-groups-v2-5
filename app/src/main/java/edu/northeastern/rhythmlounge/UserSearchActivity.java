package edu.northeastern.rhythmlounge;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the activity for searching for other users.
 */
public class UserSearchActivity extends AppCompatActivity {

    private EditText userSearchEditText;
    private RecyclerView userSuggestionRecyclerView;
    private UserSuggestionAdapter userAdapter;
    private List<User> userSuggestions = new ArrayList<>();

    private List<String> userIds = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_search);

        db = FirebaseFirestore.getInstance();

        userSearchEditText = findViewById(R.id.userSearchEditText);
        userSuggestionRecyclerView = findViewById(R.id.userSuggestionsRecyclerView);

        userAdapter = new UserSuggestionAdapter(this, userSuggestions, userIds);
        userSuggestionRecyclerView.setAdapter(userAdapter);
        userSuggestionRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(userSuggestionRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        userSuggestionRecyclerView.addItemDecoration(dividerItemDecoration);

        // Adds a text change listener for searching for users in real time
        userSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString()); // Searches for users based on input
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Searches users based on the given query and updates the suggestion list.
     * @param query The query string used for searching users.
     */

    private void searchUsers(String query) {
        if (query.trim().isEmpty()) {
            userSuggestionRecyclerView.setVisibility(View.GONE); // Keeps suggestions hidden if the query is empty.
            return;
        }

        db.collection("users")
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + "\uf8ff")
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userSuggestions.clear();
                    userIds.clear();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        User user = documentSnapshot.toObject(User.class);
                        userSuggestions.add(user);
                        userIds.add(documentSnapshot.getId());
                    }
                    userAdapter.notifyDataSetChanged();
                    userSuggestionRecyclerView.setVisibility(View.VISIBLE);
                });
    }
}
