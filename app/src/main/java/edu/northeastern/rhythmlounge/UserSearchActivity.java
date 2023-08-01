package edu.northeastern.rhythmlounge;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserSearchActivity extends AppCompatActivity {

    private EditText userSearchEditText;
    private RecyclerView userSuggestionRecyclerView;
    private UserSuggestionAdapter userAdapter;
    private List<User> userSuggestions = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_search);

        db = FirebaseFirestore.getInstance();

        userSearchEditText = findViewById(R.id.userSearchEditText);
        userSuggestionRecyclerView = findViewById(R.id.userSuggestionsRecyclerView);

        userAdapter = new UserSuggestionAdapter(this, userSuggestions);
        userSuggestionRecyclerView.setAdapter(userAdapter);
        userSuggestionRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        userSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void searchUsers(String query) {
        if (query.trim().isEmpty()) {
            userSuggestionRecyclerView.setVisibility(View.GONE);
            return;
        }

        db.collection("users")
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + "\uf8ff")
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userSuggestions.clear();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

                    }
                    userAdapter.notifyDataSetChanged();
                    userSuggestionRecyclerView.setVisibility(View.VISIBLE);
                });

    }
}
