package edu.northeastern.rhythmlounge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class EventDetailsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_event_details, container, false);

        Bundle args = getArguments();
        if (args != null) {
            String eventName = args.getString("event_name");
            String location = args.getString("location");
            String venue = args.getString("venue");
            String description = args.getString("description");
            String outsideLink = args.getString("outside_link");
            String date = args.getString("date");
            String time = args.getString("time");

            TextView textViewEventName = rootView.findViewById(R.id.textViewEventName);
            TextView textViewLocation = rootView.findViewById(R.id.textViewLocation);


            textViewEventName.setText(eventName);
            textViewLocation.setText(location);
        }

        return rootView;
    }
}



