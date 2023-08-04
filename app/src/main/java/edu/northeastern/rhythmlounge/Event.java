package edu.northeastern.rhythmlounge;

public class Event {
    private String eventId;
    private String eventName;
    private String location;
    private String description;
    private String date;
    private String time;
    private String imageUrl;

    public Event() {
    }

    public Event(String eventId, String eventName, String location,
                 String description, String date, String time, String imageUrl) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.location = location;
        this.description = description;
        this.date = date;
        this.time = time;
        this.imageUrl = imageUrl;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}

