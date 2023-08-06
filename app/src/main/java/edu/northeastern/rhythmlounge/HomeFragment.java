package edu.northeastern.rhythmlounge;

import android.annotation.SuppressLint;
import android.os.Bundle;
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

    public HomeFragment() {
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        textViewGreeting = view.findViewById(R.id.textViewGreeting);
        RecyclerView recyclerViewDiscoverEventsConcerts = view.findViewById(R.id.recyclerViewDiscoverEventsConcerts);
        discoverEventsConcertsList = new ArrayList<>();
        eventsAdapter = new EventsAdapter(discoverEventsConcertsList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewDiscoverEventsConcerts.setLayoutManager(layoutManager);
        recyclerViewDiscoverEventsConcerts.setAdapter(eventsAdapter);

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

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");

        fetchDiscoverEventsConcerts();

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchDiscoverEventsConcerts() {
        eventsRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        discoverEventsConcertsList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Event event = document.toObject(Event.class);
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


