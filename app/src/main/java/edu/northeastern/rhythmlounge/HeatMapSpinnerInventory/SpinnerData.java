package edu.northeastern.rhythmlounge.HeatMapSpinnerInventory;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.rhythmlounge.R;

public class SpinnerData {

    public static List<SpinnerOptions> getSpinnerOptions(){

        List<SpinnerOptions> spinnerOptions = new ArrayList<>();

        SpinnerOptions myLocation = new SpinnerOptions();
        myLocation.setName("My Location");
        myLocation.setImage(R.drawable.mylocation);
        spinnerOptions.add(myLocation);

        SpinnerOptions allEvents = new SpinnerOptions();
        allEvents.setName("All Events");
        allEvents.setImage(R.drawable.defaulteventpicture);
        spinnerOptions.add(allEvents);

        SpinnerOptions myFollowing = new SpinnerOptions();
        myFollowing.setName("My Following");
        myFollowing.setImage(R.drawable.defaultprofilepicture);
        spinnerOptions.add(myFollowing);

        return spinnerOptions;

    }
}
