package edu.northeastern.rhythmlounge.HeatMapSpinnerInventory;

import java.io.Serializable;

public class SpinnerOptions  implements Serializable {

    private String name;
    private int image;

    public SpinnerOptions() {
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
