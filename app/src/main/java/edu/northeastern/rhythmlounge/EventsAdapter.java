package edu.northeastern.rhythmlounge;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * EventsAdapter is an Adapter used to populate a RecyclerView with Event items in the Events Fragment.
 */
public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {
    private final List<Event> eventList;

    /**
     * Constructor for the EventsAdapter.
     * @param eventList a list of Events to be displayed.
     */
    public EventsAdapter(List<Event> eventList) {
        this.eventList = eventList;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     * @param parent the parent view group.
     * @param viewType the view type.
     * @return a new ViewHolder.
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(itemView);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * @param holder the ViewHolder to be updated.
     * @param position the position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        // update the view holder with essential event details
        holder.textViewEventName.setText(event.getEventName());
        holder.textViewLocation.setText(event.getLocation());
        holder.textViewVenue.setText(event.getVenue());
        holder.textViewDate.setText(event.getDate());
        holder.textViewTime.setText(event.getTime());


        String imageURL = event.getImageURL();
            // Load the image from URL with glide.
            if (imageURL != null && !imageURL.isEmpty()) {
                Glide.with(holder.imageViewEvent.getContext())
                    .load(imageURL)
                    .placeholder(R.drawable.concert)
                    .into(holder.imageViewEvent);
            } else {
                // Set default event image when imageUrl is empty.
                holder.imageViewEvent.setImageResource(R.drawable.concert);
            }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return the total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView textViewEventName;
        TextView textViewLocation;
        TextView textViewVenue;
        TextView textViewDate;
        TextView textViewTime;
        ImageView imageViewEvent;

        /**
         * Constructor for the EventViewHolder.
         * @param itemView the item view.
         */
        EventViewHolder(View itemView) {
            super(itemView);
            // Initialize all the view elements
            textViewEventName = itemView.findViewById(R.id.textViewEventName);

            textViewLocation = itemView.findViewById(R.id.textViewLocation);
            textViewVenue = itemView.findViewById(R.id.textViewVenue);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            imageViewEvent = itemView.findViewById(R.id.imageViewEvent);
        }
    }
}
