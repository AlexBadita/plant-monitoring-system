package com.app.plantmonitoring.database.devices;

public class Measurements {
    private int humidity;
    private int light;
    private int moisture;
    private int temperature;
    private int water;

    public Measurements(){}

    public Measurements(int humidity, int light, int moisture, int temperature, int water) {
        this.humidity = humidity;
        this.light = light;
        this.moisture = moisture;
        this.temperature = temperature;
        this.water = water;
    }

    public int getHumidity() {
        return humidity;
    }

    public int getLight() {
        return light;
    }

    public int getMoisture() {
        return moisture;
    }

    public int getTemperature() {
        return temperature;
    }

    public int getWater() {
        return water;
    }
}
