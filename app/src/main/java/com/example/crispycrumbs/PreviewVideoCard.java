package com.example.crispycrumbs;

public class PreviewVideoCard {
    public String title;
    public int image;

    public PreviewVideoCard(String title, int image) {
        this.title = title;
        this.image = image;
    }


    public String getTitle() {
        return title;
    }

    public int getImage() {
        return image;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
