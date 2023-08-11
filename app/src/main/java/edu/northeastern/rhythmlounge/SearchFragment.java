package edu.northeastern.rhythmlounge;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
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

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.rhythmlounge.Events.Event;

public class SearchFragment extends Fragment {

    private RecyclerView userSuggestionRecyclerView, eventSuggestionRecyclerView;
    private UserSuggestionAdapter userAdapter;

    private EventSuggestionAdapter eventAdapter;
    private final List<User> userSuggestions = new ArrayList<>();
    private final List<String> userIds = new ArrayList<>();

    private final List<Event> eventSuggestions = new ArrayList<>();

    private final List<String> eventIds = new ArrayList<>();
    private FirebaseFirestore db;
    private Handler searchHandler = new Handler();

    private static final long SEARCH_DELAY = 500; // 500ms delay

    public SearchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        EditText userSearchEditText = rootView.findViewById(R.id.userSearchEditText);

        userSuggestionRecyclerView = rootView.findViewById(R.id.userSuggestionsRecyclerView);
        userAdapter = new UserSuggestionAdapter(requireContext(), userSuggestions, userIds);
        userSuggestionRecyclerView.setAdapter(userAdapter);
        userSuggestionRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        userSuggestionRecyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        EditText eventSearchEditText = rootView.findViewById(R.id.eventSearchEditText);

        eventSuggestionRecyclerView = rootView.findViewById(R.id.eventSuggestionsRecyclerView);
        eventAdapter = new EventSuggestionAdapter(requireContext(), eventSuggestions, eventIds);
        eventSuggestionRecyclerView.setAdapter(eventAdapter);
        eventSuggestionRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        eventSuggestionRecyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));


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


        eventSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchEvents(s.toString());
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

        query = query.toLowerCase().trim();
        if (query.isEmpty()) {
            userSuggestionRecyclerView.setVisibility(View.GONE);
            return;
        }

        db.collection("users")
                .whereGreaterThanOrEqualTo("username_lowercase", query)
                .whereLessThanOrEqualTo("username_lowercase", query + "\uf8ff")
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

    private void searchEvents(String query) {

        query = query.toLowerCase().trim();
        if (query.trim().isEmpty()) {
            eventSuggestionRecyclerView.setVisibility(View.GONE);
            return;
        }

        Task<QuerySnapshot> task1 = db.collection("events")
                .whereGreaterThanOrEqualTo("eventName_lowercase", query)
                .whereLessThanOrEqualTo("eventName_lowercase", query + "\uf8ff")
                .limit(10)
                .get();

        Task<QuerySnapshot> task2 = db.collection("events")
                .whereGreaterThanOrEqualTo("location_lowercase", query)
                .whereLessThanOrEqualTo("location_lowercase", query + "\uf8ff")
                .limit(10)
                .get();

        Task<QuerySnapshot> task3 = db.collection("events")
                .whereGreaterThanOrEqualTo("venue_lowercase", query)
                .whereLessThanOrEqualTo("venue_lowercase", query + "\uf8ff")
                .limit(10)
                .get();

        Task<List<Object>> combinedTask = Tasks.whenAllSuccess(task1, task2, task3).addOnSuccessListener(objects -> {
            eventSuggestions.clear();
            eventIds.clear();
            for (Object object : objects) {
                for (QueryDocumentSnapshot document : (QuerySnapshot) object) {
                        Event event = document.toObject(Event.class);
                        if (!eventIds.contains(document.getId())) {
                            eventSuggestions.add(event);
                            eventIds.add(document.getId());
                    }
                }
            }
            eventAdapter.notifyDataSetChanged();
            eventSuggestionRecyclerView.setVisibility(View.VISIBLE);
        });
    }


}
