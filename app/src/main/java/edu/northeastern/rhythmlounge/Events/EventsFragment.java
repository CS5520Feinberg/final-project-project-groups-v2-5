package edu.northeastern.rhythmlounge.Events;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import edu.northeastern.rhythmlounge.R;
import edu.northeastern.rhythmlounge.RecyclerViewEventSpace;

public class EventsFragment extends Fragment implements EventsAdapter.OnItemClickListener {
    private CollectionReference eventsRef;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_events, container, false);
        Log.d("EventsFragment", "onCreateView: Called");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");

        fetchEvents();

        return rootView;
    }

    @Override
    public void onItemClick(Event event) {
        Log.d("EventsFragment", "Item clicked: " + event.getEventName());
        Intent intent  = new Intent(getActivity(), EventDetailsActivity.class);
        intent.putExtra("event_name", event.getEventName());
        intent.putExtra("location", event.getLocation());
        intent.putExtra("venue", event.getVenue());
        intent.putExtra("description", event.getDescription());
        intent.putExtra("outside_link", event.getOutsideLink());
        intent.putExtra("date", event.getDate());
        intent.putExtra("time", event.getTime());
        intent.putExtra("imageURL", event.getImageURL());
        intent.putExtra("eventId", event.getDocId());
        Log.d("EventsFragment", "Passing eventId: " + event.getDocId());

        startActivity(intent);
    }
    private void fetchEvents() {
        eventsRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Event> upcomingConcerts = new ArrayList<>();
                        List<Event> upcomingEvents = new ArrayList<>();
                        List<Event> otherConcerts = new ArrayList<>();
                        List<Event> otherEvents = new ArrayList<>();

                        String currentMonth = new SimpleDateFormat("MM", Locale.getDefault()).format(Calendar.getInstance().getTime());

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Event event = document.toObject(Event.class);
                            event.setDocId(document.getId());
                            String eventMonth = event.getDate().substring(0, 2);

                            if (event.getIsConcert()) {
                                if (eventMonth.equals(currentMonth)) {
                                    upcomingConcerts.add(event);
                                } else {
                                    otherConcerts.add(event);
                                }
                            } else {
                                if (eventMonth.equals(currentMonth)) {
                                    upcomingEvents.add(event);
                                } else {
                                    otherEvents.add(event);
                                }
                            }
                        }

                        RecyclerView recyclerViewConcerts = rootView.findViewById(R.id.recyclerViewConcertsHappening);
                        LinearLayoutManager concertsLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
                        recyclerViewConcerts.setLayoutManager(concertsLayoutManager);
                        recyclerViewConcerts.setAdapter(initializeAdapter(upcomingConcerts));

                        RecyclerView recyclerViewOtherConcerts = rootView.findViewById(R.id.recyclerViewNewConcerts);
                        LinearLayoutManager otherConcertsLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
                        recyclerViewOtherConcerts.setLayoutManager(otherConcertsLayoutManager);
                        recyclerViewOtherConcerts.setAdapter(initializeAdapter(otherConcerts));

                        RecyclerView recyclerViewEvents = rootView.findViewById(R.id.recyclerViewEventsHappening);
                        LinearLayoutManager eventsLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
                        recyclerViewEvents.setLayoutManager(eventsLayoutManager);
                        recyclerViewEvents.setAdapter(initializeAdapter(upcomingEvents));

                        RecyclerView recyclerViewOtherEvents = rootView.findViewById(R.id.recyclerViewNewEvents);
                        LinearLayoutManager otherEventsLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
                        recyclerViewOtherEvents.setLayoutManager(otherEventsLayoutManager);
                        recyclerViewOtherEvents.setAdapter(initializeAdapter(otherEvents));
                    }
                });


    }

    private EventsAdapter initializeAdapter(List<Event> eventsList) {
        EventsAdapter adapter = new EventsAdapter(eventsList);
        adapter.setOnItemClickListener(this);
        return adapter;
    }

}
