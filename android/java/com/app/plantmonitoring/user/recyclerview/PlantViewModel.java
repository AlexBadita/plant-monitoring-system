package com.app.plantmonitoring.user.recyclerview;

public class PlantViewModel {
    private String id;
    private String name;
    private String imageUrl;

    public PlantViewModel(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public PlantViewModel(String id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getId(){
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return imageUrl;
    }

    public void setImage(String image) {
        this.imageUrl = image;
    }
}
