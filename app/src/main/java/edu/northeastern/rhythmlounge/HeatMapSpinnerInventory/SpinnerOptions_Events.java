package edu.northeastern.rhythmlounge.HeatMapSpinnerInventory;

import java.io.Serializable;

import edu.northeastern.rhythmlounge.R;

public class SpinnerOptions_Events implements Serializable {

    private String event_name;
    private int event_image;
    private String event_id;

    public SpinnerOptions_Events() {
    }

    public String getEvent_name() {
        return event_name;
    }

    public void setEvent_name(String event_name) {
        this.event_name = event_name;
    }

    public int getEvent_image() {
        return event_image;
    }

    public void setEvent_image(int event_image) {
        this.event_image = event_image;
    }

    public String getEvent_id() {
        return event_id;
    }

    public void setEvent_id(String event_id) {
        this.event_id = event_id;
    }
}
