package edu.northeastern.rhythmlounge.Playlist;

/**
 * This class represents a song object, which has a title, a artist, and a youtubeLink.
 */
public class Song {
    private String title;
    private String artist;
    private String youtubeLink;

    public Song() {}

    public Song(String title, String artist, String youtubeLink) {
        this.title = title;
        this.artist = artist;
        this.youtubeLink = youtubeLink;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getYoutubeLink() {
        return youtubeLink;
    }

    public void setYoutubeLink(String youtubeLink) {
        this.youtubeLink = youtubeLink;
    }
}
