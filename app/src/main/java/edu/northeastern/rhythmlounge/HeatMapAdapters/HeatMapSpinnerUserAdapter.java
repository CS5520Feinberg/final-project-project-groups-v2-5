package edu.northeastern.rhythmlounge.HeatMapAdapters;

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

import edu.northeastern.rhythmlounge.R;

public class HeatMapSpinnerUserAdapter extends BaseAdapter {
    private static final String TAG = "Activity____SPINNER_USER";

    private Context context;

    private List<DocumentSnapshot> spinnerOptions_users;

    public HeatMapSpinnerUserAdapter(Context context, List<DocumentSnapshot> spinnerOptions_users) {
        this.context = context;
        this.spinnerOptions_users = spinnerOptions_users;
    }

    @Override
    public int getCount() {
        Log.d(TAG, "getCount_users: " + spinnerOptions_users.size());
        Log.d(TAG, "HeatMapSpinnerAdapter_users: "+ spinnerOptions_users);

        return spinnerOptions_users != null ? spinnerOptions_users.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return spinnerOptions_users.get(position);
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
        DocumentSnapshot snapshot = spinnerOptions_users.get(position);
        String userName = snapshot.getString("username");
        int eventImageResId = R.drawable.defaultprofilepicture;

        txtName.setText(userName);
        image.setImageResource(eventImageResId);

        return rootView;
    }
}
