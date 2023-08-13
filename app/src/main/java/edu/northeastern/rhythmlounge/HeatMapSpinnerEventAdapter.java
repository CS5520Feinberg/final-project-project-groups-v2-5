package edu.northeastern.rhythmlounge;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

import edu.northeastern.rhythmlounge.HeatMapSpinnerInventory.SpinnerOptions_Events;

public class HeatMapSpinnerEventAdapter extends BaseAdapter {

    private static final String TAG = "Activity____SPINNER";

    private Context context;

    private List<DocumentSnapshot> spinnerOptions_events;

    public HeatMapSpinnerEventAdapter(Context context, List<DocumentSnapshot> spinnerOptions_events) {
        this.context = context;
        this.spinnerOptions_events = spinnerOptions_events;
    }

    @Override
    public int getCount() {
        Log.d(TAG, "getCount_event: " + spinnerOptions_events.size());
        Log.d(TAG, "HeatMapSpinnerAdapter_event: "+ spinnerOptions_events);

        return spinnerOptions_events != null ? spinnerOptions_events.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return spinnerOptions_events.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View rootView = LayoutInflater.from(context)
                .inflate(R.layout.custom_spinner_events, viewGroup, false);

        TextView txtName = rootView.findViewById(R.id.spinnernameevent);
        ImageView image = rootView.findViewById(R.id.spinnerimageevent);
        DocumentSnapshot snapshot = spinnerOptions_events.get(position);
        String eventName = snapshot.getString("eventName");
        int eventImageResId = R.drawable.defaulteventpicture;

        txtName.setText(eventName);
        image.setImageResource(eventImageResId);

        return rootView;
    }
}
