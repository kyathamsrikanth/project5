package edu.uic.srikanthk.cs478.funcenter;


// all data that needs to be sent
public class Info {
    private String title;
    private String artist;
    private String image;

    // constructor
    public Info(String title, String artist, String image) {
        this.title = title;
        this.artist = artist;
        this.image = image;
    }

    public String getInfo() {
        return this.title + "&" + this.artist + "&" + this.image;
    }
}

