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

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {
    private List<Event> eventList;

    public EventsAdapter(List<Event> eventList) {
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.textViewEventName.setText(event.getEventName());
        holder.textViewLocation.setText(event.getLocation());
        holder.textViewDate.setText(event.getDate());
        holder.textViewTime.setText(event.getTime());


        Glide.with(holder.imageViewEvent.getContext())
                .load(event.getImageUrl())
                .placeholder(R.drawable.concert)
                .into(holder.imageViewEvent);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView textViewEventName;
        TextView textViewLocation;
        TextView textViewDate;
        TextView textViewTime;
        ImageView imageViewEvent;

        EventViewHolder(View itemView) {
            super(itemView);
            textViewEventName = itemView.findViewById(R.id.textViewEventName);
            textViewLocation = itemView.findViewById(R.id.textViewLocation);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            imageViewEvent = itemView.findViewById(R.id.imageViewEvent);
        }
    }
}
