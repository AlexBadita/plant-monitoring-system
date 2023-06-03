package com.app.plantmonitoring.database.devices;

public class History {
    private String light;
    private String humidity;
    private String temperature;
    private String moisture;

    public History() {}

    public History(String light, String humidity, String temperature, String moisture) {
        this.light = light;
        this.humidity = humidity;
        this.temperature = temperature;
        this.moisture = moisture;
    }

    public String getLight() {
        return light;
    }

    public void setLight(String light) {
        this.light = light;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getMoisture() {
        return moisture;
    }

    public void setMoisture(String moisture) {
        this.moisture = moisture;
    }
}
