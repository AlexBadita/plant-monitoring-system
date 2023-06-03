package com.app.plantmonitoring.database.plants;

public class Plant {
    private String name;
    private String humidityMin;
    private String humidityMax;
    private String temperatureMin;
    private String temperatureMax;
    private String lightMin;
    private String lightMax;
    private String moisture;

    public Plant() { }

    public Plant(String name, String humidityMin, String humidityMax, String temperatureMin, String temperatureMax, String lightMin, String lightMax, String moisture) {
        this.name = name;
        this.humidityMin = humidityMin;
        this.humidityMax = humidityMax;
        this.temperatureMin = temperatureMin;
        this.temperatureMax = temperatureMax;
        this.lightMin = lightMin;
        this.lightMax = lightMax;
        this.moisture = moisture;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHumidityMin() {
        return humidityMin;
    }

    public void setHumidityMin(String humidityMin) {
        this.humidityMin = humidityMin;
    }

    public String getHumidityMax() {
        return humidityMax;
    }

    public void setHumidityMax(String humidityMax) {
        this.humidityMax = humidityMax;
    }

    public String getTemperatureMin() {
        return temperatureMin;
    }

    public void setTemperatureMin(String temperatureMin) {
        this.temperatureMin = temperatureMin;
    }

    public String getTemperatureMax() {
        return temperatureMax;
    }

    public void setTemperatureMax(String temperatureMax) {
        this.temperatureMax = temperatureMax;
    }

    public String getLightMin() {
        return lightMin;
    }

    public void setLightMin(String lightMin) {
        this.lightMin = lightMin;
    }

    public String getLightMax() {
        return lightMax;
    }

    public void setLightMax(String lightMax) {
        this.lightMax = lightMax;
    }

    public String getMoisture() {
        return moisture;
    }

    public void setMoisture(String moisture) {
        this.moisture = moisture;
    }
}
