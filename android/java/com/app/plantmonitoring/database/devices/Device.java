package com.app.plantmonitoring.database.devices;

public class Device {
    private String id;
    private Settings settings;
    private Measurements measurements;
    private String name;
    private String imageUrl;
    private History history;

    public Device() {}

    public Device(Settings settings, Measurements measurements) {
        this.settings = settings;
        this.measurements = measurements;
    }

    public Device(String id, Settings settings, Measurements measurements, String name) {
        this.id = id;
        this.settings = settings;
        this.measurements = measurements;
        this.name = name;
    }

    public Device(String id, Settings settings, Measurements measurements, String name, History history) {
        this.id = id;
        this.settings = settings;
        this.measurements = measurements;
        this.name = name;
        this.history = history;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public Measurements getMeasurements() {
        return measurements;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMeasurements(Measurements measurements) {
        this.measurements = measurements;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public History getHistory() {
        return history;
    }

    public void setHistory(History history) {
        this.history = history;
    }
}
