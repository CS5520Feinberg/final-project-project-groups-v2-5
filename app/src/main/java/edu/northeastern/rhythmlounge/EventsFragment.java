package edu.northeastern.rhythmlounge;

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

public class EventsFragment extends Fragment implements EventsAdapter.OnItemClickListener {
    private CollectionReference eventsRef;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_events, container, false);
        Log.d("EventsFragment", "onCreateView: Called");

        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerViewConcertsHappening);

        List<Event> eventList = new ArrayList<>();
        EventsAdapter eventsAdapter = new EventsAdapter(eventList);

        eventsAdapter.setOnItemClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        int spaceBetweenItemsInDp = 4;
        int spaceBetweenItemsInPixels = (int) (spaceBetweenItemsInDp * getResources().getDisplayMetrics().density);
        recyclerView.addItemDecoration(new RecyclerViewEventSpace(spaceBetweenItemsInPixels));

        recyclerView.setAdapter(eventsAdapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");

        fetchEvents();

        return rootView;
    }

    @Override
    public void onItemClick(Event event) {

        EventDetailsFragment eventDetailsFragment = new EventDetailsFragment();

        Bundle args = new Bundle();
        args.putString("event_name", event.getEventName());
        args.putString("location", event.getLocation());
        args.putString("venue", event.getVenue());
        args.putString("description", event.getDescription());
        args.putString("outside_link", event.getOutsideLink());
        args.putString("date", event.getDate());
        args.putString("time", event.getTime());

        eventDetailsFragment.setArguments(args);

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, eventDetailsFragment)
                .addToBackStack(null)
                .commit();
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
                        EventsAdapter concertsAdapter = new EventsAdapter(upcomingConcerts);
                        LinearLayoutManager concertsLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
                        recyclerViewConcerts.setLayoutManager(concertsLayoutManager);
                        recyclerViewConcerts.setAdapter(concertsAdapter);

                        RecyclerView recyclerViewOtherConcerts = rootView.findViewById(R.id.recyclerViewNewConcerts);
                        EventsAdapter otherConcertsAdapter = new EventsAdapter(otherConcerts);
                        LinearLayoutManager otherConcertsLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
                        recyclerViewOtherConcerts.setLayoutManager(otherConcertsLayoutManager);
                        recyclerViewOtherConcerts.setAdapter(otherConcertsAdapter);

                        RecyclerView recyclerViewEvents = rootView.findViewById(R.id.recyclerViewEventsHappening);
                        EventsAdapter eventsAdapter = new EventsAdapter(upcomingEvents);
                        LinearLayoutManager eventsLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
                        recyclerViewEvents.setLayoutManager(eventsLayoutManager);
                        recyclerViewEvents.setAdapter(eventsAdapter);

                        RecyclerView recyclerViewOtherEvents = rootView.findViewById(R.id.recyclerViewNewEvents);
                        EventsAdapter otherEventsAdapter = new EventsAdapter(otherEvents);
                        LinearLayoutManager otherEventsLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
                        recyclerViewOtherEvents.setLayoutManager(otherEventsLayoutManager);
                        recyclerViewOtherEvents.setAdapter(otherEventsAdapter);

                    }
                });
    }

}
