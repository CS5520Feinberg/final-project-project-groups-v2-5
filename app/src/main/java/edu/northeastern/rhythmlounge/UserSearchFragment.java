package edu.northeastern.rhythmlounge;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserSearchFragment extends Fragment {

    private RecyclerView userSuggestionRecyclerView;
    private UserSuggestionAdapter userAdapter;
    private final List<User> userSuggestions = new ArrayList<>();

    private final List<String> userIds = new ArrayList<>();
    private FirebaseFirestore db;

    public UserSearchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_search, container, false);

        EditText userSearchEditText = rootView.findViewById(R.id.userSearchEditText);
        userSuggestionRecyclerView = rootView.findViewById(R.id.userSuggestionsRecyclerView);

        userAdapter = new UserSuggestionAdapter(requireContext(), userSuggestions, userIds);
        userSuggestionRecyclerView.setAdapter(userAdapter);
        userSuggestionRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        userSuggestionRecyclerView.addItemDecoration(dividerItemDecoration);


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

        return rootView;
    }

    /**
     * Searches users based on the given query and updates the suggestion list.
     * @param query The query string used for searching users.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void searchUsers(String query) {
        if (query.trim().isEmpty()) {
            userSuggestionRecyclerView.setVisibility(View.GONE);
            return;
        }

        db.collection("users")
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + "\uf8ff")
                .limit(10)
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
