package edu.northeastern.rhythmlounge.HeatMapSpinnerInventory;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.rhythmlounge.R;

public class SpinnerData {

    public static List<SpinnerOptions> getSpinnerOptions(){

        List<SpinnerOptions> spinnerOptions = new ArrayList<>();

        SpinnerOptions myLocation = new SpinnerOptions();
        myLocation.setName("My Location");
        myLocation.setImage(R.drawable.ic_current_location);
        spinnerOptions.add(myLocation);

        SpinnerOptions myFollowing = new SpinnerOptions();
        myLocation.setName("My Following");
        myLocation.setImage(R.drawable.defaultprofilepicture);
        spinnerOptions.add(myFollowing);

        SpinnerOptions allEvents = new SpinnerOptions();
        myLocation.setName("All Events");
        myLocation.setImage(R.drawable.defaulteventpicture);
        spinnerOptions.add(myFollowing);

        return spinnerOptions;

    }
}
