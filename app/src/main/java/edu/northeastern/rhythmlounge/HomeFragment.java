package edu.northeastern.rhythmlounge;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;


public class HomeFragment extends Fragment {

    private TextView textViewGreeting;
    private EventsAdapter eventsAdapter;
    private List<Event> discoverEventsConcertsList;
    private CollectionReference eventsRef;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public HomeFragment() {
        Log.d("HomeFragment", "Initializing HomeFragment...");
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("HomeFragment", "onCreateView called");
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        textViewGreeting = view.findViewById(R.id.textViewGreeting);
        RecyclerView recyclerViewDiscoverEventsConcerts = view.findViewById(R.id.recyclerViewDiscoverEvents);

        discoverEventsConcertsList = new ArrayList<>();
        eventsAdapter = new EventsAdapter(discoverEventsConcertsList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewDiscoverEventsConcerts.setLayoutManager(layoutManager);
        recyclerViewDiscoverEventsConcerts.setAdapter(eventsAdapter);

        setupGreeting();
        setupEventClickListeners();
        fetchDiscoverEventsConcerts();

        return view;
    }

    private void setupGreeting() {
        String greeting = getGreetingBasedOnTimeOfDay();
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        String fullGreeting = greeting + " " + username + "!";
                        textViewGreeting.setText(fullGreeting);
                    } else {
                        textViewGreeting.setText("Good Afternoon Guest!");
                    }
                })
                .addOnFailureListener(e -> textViewGreeting.setText("Good Afternoon Guest!"));

    }

    private void setupEventClickListeners() {
        Log.d("HomeFragment", "Setting up event click listener");
        eventsAdapter.setOnItemClickListener(event -> {
            Log.d("HomeFragment", "Event clicked with ID: " + event.getDocId());
            Intent intent = new Intent(getContext(), EventDetailsActivity.class);
            intent.putExtra("eventId", event.getDocId());
            intent.putExtra("event_name", event.getEventName());
            intent.putExtra("location", event.getLocation());
            intent.putExtra("venue", event.getVenue());
            intent.putExtra("description", event.getDescription());
            intent.putExtra("outside_link", event.getOutsideLink());
            intent.putExtra("date", event.getDate());
            intent.putExtra("time", event.getTime());
            intent.putExtra("imageURL", event.getImageURL());
            Log.d("EventsFragment", "Passing eventId: " + event.getDocId());
            startActivity(intent);
        });
    }



    @SuppressLint("NotifyDataSetChanged")
    private void fetchDiscoverEventsConcerts() {
        eventsRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        discoverEventsConcertsList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Event event = document.toObject(Event.class);
                            event.setDocId(document.getId());
                            discoverEventsConcertsList.add(event);
                        }
                        eventsAdapter.notifyDataSetChanged();
                    }
                });
    }

    private String getGreetingBasedOnTimeOfDay() {
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;

        if (hourOfDay >= 5 && hourOfDay < 12) {
            greeting = "Good Morning";
        } else if (hourOfDay >= 12 && hourOfDay < 18) {
            greeting = "Good Afternoon";
        } else {
            greeting = "Good Evening";
        }

        return greeting;
    }
}


