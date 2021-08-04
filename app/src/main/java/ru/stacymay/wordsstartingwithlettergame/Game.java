package ru.stacymay.wordsstartingwithlettergame;

public class Game {

    private String id;
    private String organizerName;
    private String organizerPhotoUrl;

    public Game(String id, String organizerName, String organizerPhotoUrl) {
        this.id = id;
        this.organizerName = organizerName;
        this.organizerPhotoUrl = organizerPhotoUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerId) {
        this.organizerName = organizerId;
    }

    public String getOrganizerPhotoUrl() {
        return organizerPhotoUrl;
    }

    public void setOrganizerPhotoUrl(String organizerPhotoUrl) {
        this.organizerPhotoUrl = organizerPhotoUrl;
    }
}
