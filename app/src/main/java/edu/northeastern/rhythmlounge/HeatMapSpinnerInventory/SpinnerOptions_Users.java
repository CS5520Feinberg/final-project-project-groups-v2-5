package edu.northeastern.rhythmlounge.HeatMapSpinnerInventory;

import java.io.Serializable;

public class SpinnerOptions_Users implements Serializable {
    private String user_name;
    private int user_image;
    private String user_id;

    public SpinnerOptions_Users() {
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public int getUser_image() {
        return user_image;
    }

    public void setUser_image(int user_image) {
        this.user_image = user_image;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
