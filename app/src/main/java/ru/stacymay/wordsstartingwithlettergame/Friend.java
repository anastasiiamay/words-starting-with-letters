package ru.stacymay.wordsstartingwithlettergame;

public class Friend {

    private String name;
    private String id;
    private String photoUrl;
    private boolean chosen;
    private boolean isOnline;

    public Friend(String name, String id, String photoUrl, boolean isOnline) {
        this.name = name;
        this.id = id;
        this.photoUrl = photoUrl;
        this.chosen = false;
        this.isOnline = isOnline;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public boolean isChosen() {
        return chosen;
    }

    public void setChosen(boolean chosen) {
        this.chosen = chosen;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}
