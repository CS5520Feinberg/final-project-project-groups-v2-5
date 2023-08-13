package edu.northeastern.rhythmlounge;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import edu.northeastern.rhythmlounge.HeatMapSpinnerInventory.SpinnerOptions;
import edu.northeastern.rhythmlounge.HeatMapSpinnerInventory.SpinnerOptions_Events;

public class HeatMapSpinnerAdapter extends BaseAdapter {

    private static final String TAG = "Activity____SPINNER";

    private Context context;
    private List<SpinnerOptions> spinnerOptionsList;

    public HeatMapSpinnerAdapter(Context context, List<SpinnerOptions> spinnerOptionsList) {
        this.context = context;
        this.spinnerOptionsList = spinnerOptionsList;
    }


    @Override
    public int getCount() {
        Log.d(TAG, "getCount: " + spinnerOptionsList.size());
        Log.d(TAG, "HeatMapSpinnerAdapter: "+ spinnerOptionsList);

        return spinnerOptionsList != null ? spinnerOptionsList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View rootView = LayoutInflater.from(context)
                .inflate(R.layout.custom_spinner, viewGroup, false);

        TextView txtName = rootView.findViewById(R.id.spinnername);
        ImageView image = rootView.findViewById(R.id.spinnerimage);

        txtName.setText(spinnerOptionsList.get(position).getName());
        image.setImageResource(spinnerOptionsList.get(position).getImage());

        return rootView;
    }
}
