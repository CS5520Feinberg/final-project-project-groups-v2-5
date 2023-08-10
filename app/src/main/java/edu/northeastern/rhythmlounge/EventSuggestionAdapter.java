package edu.northeastern.rhythmlounge;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import edu.northeastern.rhythmlounge.Events.Event;
import edu.northeastern.rhythmlounge.Events.EventDetailsActivity;

/**
 * Adapter for displaying event suggestions in a RecyclerView.
 */
public class EventSuggestionAdapter extends RecyclerView.Adapter<EventSuggestionAdapter.ViewHolder> {

    private final List<Event> events;
    private final List<String> eventIds;
    private final Context context;

    /**
     * Constructs a new EventSuggestionAdapter.
     * @param context  the activity or application context.
     * @param events   the list of events to display
     * @param eventIds the list of event IDs
     */
    public EventSuggestionAdapter(Context context, List<Event> events, List<String> eventIds ) {
        this.events = events;
        this.eventIds = eventIds;
        this.context = context;
    }

    /**
     * Inflates the view for each event item.
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return the new ViewHolder
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_suggestion, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds data to the view for each event item.
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);

        holder.eventNameText.setText(event.getEventName());
        holder.venueText.setText(event.getVenue());
        holder.eventLocationText.setText(event.getLocation());
        holder.eventDateText.setText(event.getDate());
        holder.eventTimeText.setText(event.getTime());

        if (event.getImageURL() != null && !event.getImageURL().isEmpty()) {
            Glide.with(context)
                    .load(event.getImageURL())
                    .into(holder.eventImage);
        } else {
            Glide.with(context)
                    .load(R.drawable.defaulteventpicture)
                    .into(holder.eventImage);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailsActivity.class);
            intent.putExtra("eventId", eventIds.get(position));
            intent.putExtra("event_name", event.getEventName());
            intent.putExtra("location", event.getLocation());
            intent.putExtra("venue", event.getVenue());
            intent.putExtra("description", event.getDescription());
            intent.putExtra("outside_link", event.getOutsideLink());
            intent.putExtra("date", event.getDate());
            intent.putExtra("time", event.getTime());
            intent.putExtra("imageURL", event.getImageURL());
            Log.d("EventsFragment", "Passing eventId: " + event.getDocId());
            context.startActivity(intent);

        });
    }

    /**
     * Returns the total number of events in the list.
     * @return the number of events.
     */
    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * ViewHolder class for each event item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView eventImage;
        final TextView eventNameText;
        final TextView venueText;
        final TextView eventLocationText;
        final TextView eventDateText;
        final TextView eventTimeText;

        /**
         * Constructs a ViewHolder for an event item view.
         * @param view the event item view.
         */
        public ViewHolder(View view) {
            super(view);
            eventImage = view.findViewById(R.id.eventImage);
            eventNameText = view.findViewById(R.id.eventNameText);
            venueText = view.findViewById(R.id.venueText);
            eventLocationText = view.findViewById(R.id.eventLocationText);
            eventDateText = view.findViewById(R.id.eventDateText);
            eventTimeText = view.findViewById(R.id.eventTimeText);
        }
    }
}
