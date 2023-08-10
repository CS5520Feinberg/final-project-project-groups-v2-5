package edu.northeastern.rhythmlounge;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;


public class HomeFragment extends Fragment {

    private TextView textViewGreeting;
    private EventsAdapter discoverEventsConcertsAdapter, myEventsAdapter, myConcertsAdapter;
    private List<Event> discoverEventsConcertsList, myConcertsList, myEventsList;

    ProgressBar progressBarDiscoverEventsConcerts, progressBarMyEvents, progressBarMyConcerts;
    private final CollectionReference eventsRef;
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;

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

        // Loaders for the RecyclerViews
        progressBarDiscoverEventsConcerts = view.findViewById(R.id.progressBarDiscoverEventsConcerts);
        progressBarMyEvents = view.findViewById(R.id.progressBarMyEvents);
        progressBarMyConcerts = view.findViewById(R.id.progressBarMyConcerts);

        // The Discover Events/Concerts section:
        RecyclerView recyclerViewDiscoverEventsConcerts = view.findViewById(R.id.recyclerViewDiscoverEvents);
        discoverEventsConcertsList = new ArrayList<>();
        discoverEventsConcertsAdapter = new EventsAdapter(discoverEventsConcertsList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewDiscoverEventsConcerts.setLayoutManager(layoutManager);
        recyclerViewDiscoverEventsConcerts.setAdapter(discoverEventsConcertsAdapter);

        // My events section
        RecyclerView recyclerViewMyEvents = view.findViewById(R.id.recyclerViewMyEvents);
        myEventsList = new ArrayList<>();
        myEventsAdapter = new EventsAdapter(myEventsList);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewMyEvents.setLayoutManager(layoutManager1);
        recyclerViewMyEvents.setAdapter(myEventsAdapter);

        // My concerts section
        RecyclerView recyclerViewMyConcerts = view.findViewById(R.id.recyclerViewMyConcerts);
        myConcertsList = new ArrayList<>();
        myConcertsAdapter = new EventsAdapter(myConcertsList);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewMyConcerts.setLayoutManager(layoutManager2);
        recyclerViewMyConcerts.setAdapter(myConcertsAdapter);

        setupGreeting();
        setupEventClickListeners(); // Click listeners for each event
        fetchDiscoverEventsConcerts();  // Fetch the all events.
        fetchMyEventsAndConcerts(); // Fetch the events and concerts.

        return view;
    }

    /**
     * Sets up the greeting based on the time of day and user details.
     */
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

    /**
     * Sets up event click listeners for events lists.
     */
    private void setupEventClickListeners() {
        Log.d("HomeFragment", "Setting up event click listener");

        EventsAdapter.OnItemClickListener listener = event -> {
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
        };

        discoverEventsConcertsAdapter.setOnItemClickListener(listener);
        myEventsAdapter.setOnItemClickListener(listener);
        myConcertsAdapter.setOnItemClickListener(listener);
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
                        discoverEventsConcertsAdapter.notifyDataSetChanged();
                    }

                    progressBarDiscoverEventsConcerts.setVisibility(View.GONE);
                });
    }

    private void fetchMyEventsAndConcerts() {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser().getUid());

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                   if (documentSnapshot.exists()) {
                       List<String> rsvpdEvents = (List<String>) documentSnapshot.get("rsvpd");

                       if (rsvpdEvents != null && !rsvpdEvents.isEmpty()) {
                           myEventsList.clear();

                           AtomicInteger counter = new AtomicInteger(rsvpdEvents.size());

                           for (String eventId : rsvpdEvents) {
                               eventsRef.document(eventId)
                                       .get()
                                       .addOnSuccessListener(eventDocument -> {
                                           if (eventDocument.exists() && !eventDocument.getBoolean("isConcert")) {
                                               Event event = eventDocument.toObject(Event.class);
                                               event.setDocId(eventDocument.getId());
                                               myEventsList.add(event);
                                           }
                                           if (eventDocument.exists() && eventDocument.getBoolean("isConcert")) {
                                               Event event = eventDocument.toObject(Event.class);
                                               event.setDocId(eventDocument.getId());
                                               myConcertsList.add(event);
                                           }

                                           if (counter.decrementAndGet() == 0) {
                                               myEventsAdapter.notifyDataSetChanged();
                                               myConcertsAdapter.notifyDataSetChanged();
                                               progressBarMyEvents.setVisibility(View.GONE);
                                               progressBarMyConcerts.setVisibility(View.GONE);
                                           }
                                       });
                           }
                       } else {
                           progressBarMyEvents.setVisibility(View.GONE);
                           progressBarMyConcerts.setVisibility(View.GONE);
                       }
                   } else {
                       progressBarMyConcerts.setVisibility(View.GONE);
                       progressBarMyConcerts.setVisibility(View.GONE);
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


