package edu.northeastern.rhythmlounge.Playlists;

import java.util.List;

/**
 * This class represents a Playlist object which has a name, and a list of Song objects.
 */
public class Playlist {

    private String name;

    private List<Song> songs;

    public Playlist() {}

    public Playlist(String name, List<Song> songs) {
        this.name = name;
        this.songs = songs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }
}
