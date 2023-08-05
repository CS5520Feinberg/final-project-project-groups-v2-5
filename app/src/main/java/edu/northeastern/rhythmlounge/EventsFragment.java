package edu.northeastern.rhythmlounge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EventsFragment extends Fragment {
    private RecyclerView recyclerView;
    private EventsAdapter eventsAdapter;
    private List<Event> eventList;
    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_events, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerViewEvents);
        eventList = new ArrayList<>();
        eventsAdapter = new EventsAdapter(eventList);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(eventsAdapter);

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");

        fetchEvents();

        return rootView;
    }

    private void fetchEvents() {
        eventsRef.get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    eventList.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Event event = document.toObject(Event.class);
                        eventList.add(event);
                    }
                    eventsAdapter.notifyDataSetChanged();
                } else {
                    // Handle failure
                }
            });
    }
}
