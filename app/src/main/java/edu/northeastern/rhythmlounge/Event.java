package edu.northeastern.rhythmlounge;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an event, containing details such as an event name, location,
 * venue, description, date, time, imageURL, eventCreator, rsvps, outsideLink.
 */
public class Event {
    private String eventName;
    private String location;

    private String venue;
    private String description;
    private String date;
    private String time;
    private String imageURL;

    private String eventCreator;

    private List<String> rsvps;

    private String outsideLink;

    /**
     * Default Constructor
     */
    public Event() {
    }

    /**
     * Creates an event with specified details.
     *
     * @param eventName    The name of the event.
     * @param location     The location of the event.
     * @param venue        The venue of the event.
     * @param description  A description of the event.
     * @param date         The date of the event.
     * @param time         The time of the event.
     * @param imageURL     A URL to an image representing the event.
     * @param eventCreator The creator of the event.
     * @param outsideLink  An external link related to the event.
     */
    public Event(String eventName, String location, String venue,
                 String description, String date, String time,
                 String imageURL, String eventCreator, String outsideLink) {
        this.eventName = eventName;
        this.location = location;
        this.venue = venue;
        this.description = description;
        this.date = date;
        this.time = time;
        this.imageURL = imageURL;
        this.eventCreator = eventCreator;
        this.rsvps = new ArrayList<>();
        this.outsideLink = outsideLink;
    }

/**
     * @return The name of the event.
     */
    public String getEventName() { return eventName; }

    /**
     * Sets the name of the event.
     *
     * @param eventName The new name of the event.
     */
    public void setEventName(String eventName) { this.eventName = eventName;}

    /**
     * @return The location of the event.
     */
    public String getLocation() { return location; }

    /**
     * Sets the location of the event.
     *
     * @param location The new location of the event.
     */
    public void setLocation(String location) { this.location = location; }

    /**
     * @return The venue of the event.
     */
    public String getVenue() { return venue; }

    /**
     * Sets the venue of the event.
     *
     * @param venue The new venue of the event.
     */
    public void setVenue(String venue) { this.venue = venue; }

    /**
     * @return A description of the event.
     */
    public String getDescription() { return description; }

    /**
     * Sets the description of the event.
     *
     * @param description The new description of the event.
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * @return The date of the event.
     */
    public String getDate() { return date; }

    /**
     * Sets the date of the event.
     *
     * @param date The new date of the event.
     */
    public void setDate(String date) { this.date = date; }

    /**
     * @return The time of the event.
     */
    public String getTime() { return time; }

    /**
     * Sets the time of the event.
     *
     * @param time The new time of the event.
     */
    public void setTime(String time) { this.time = time; }

    /**
     * @return The image URL of the event.
     */
    public String getImageURL() { return imageURL; }

    /**
     * Sets the image URL of the event.
     *
     * @param imageURL The new image URL of the event.
     */
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }

    /**
     * @return The creator of the event.
     */
    public String getEventCreator() { return eventCreator; }

    /**
     * Sets the creator of the event.
     *
     * @param eventCreator The new creator of the event.
     */
    public void setEventCreator(String eventCreator ) { this.eventCreator = eventCreator; }

    /**
     * @return The RSVPs for the event.
     */
    public List<String> getRsvps() { return rsvps; }

    /**
     * Sets the RSVPs for the event.
     *
     * @param rsvps The new RSVPs for the event.
     */
    public void setRsvps(List<String> rsvps) { this.rsvps = rsvps; }

    /**
     * @return The outside link related to the event.
     */
    public String getOutsideLink() { return outsideLink; }

    /**
     * Sets the outside link related to the event.
     *
     * @param outsideLink The new outside link related to the event.
     */
    public void setOutsideLink(String outsideLink) { this.outsideLink = outsideLink; }

}

