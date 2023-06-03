package com.app.plantmonitoring.database.devices;

public class Settings {
    private Delay delay;
    private Notification temperatureNotification;
    private Notification humidityNotification;
    private Notification lightNotification;
    private boolean changes;
    private String moistureThreshold;
    private String pumpRunTime;

    public Settings() {
    }

    public Settings(Delay delay, Notification temperatureNotification, Notification humidityNotification,
                    Notification lightNotification, String moistureThreshold, String pumpRunTime) {
        this.delay = delay;
        this.temperatureNotification = temperatureNotification;
        this.humidityNotification = humidityNotification;
        this.lightNotification = lightNotification;
        this.changes = true;
        this.moistureThreshold = moistureThreshold;
        this.pumpRunTime = pumpRunTime;
    }

    public Settings(Delay delay, Notification temperatureNotification, Notification humidityNotification,
                    Notification lightNotification, boolean changes, String moistureThreshold, String pumpRunTime) {
        this.delay = delay;
        this.temperatureNotification = temperatureNotification;
        this.humidityNotification = humidityNotification;
        this.lightNotification = lightNotification;
        this.changes = changes;
        this.moistureThreshold = moistureThreshold;
        this.pumpRunTime = pumpRunTime;
    }

    public Delay getDelay() {
        return delay;
    }

    public void setDelay(Delay delay) {
        this.delay = delay;
    }

    public Notification getTemperatureNotification() {
        return temperatureNotification;
    }

    public void setTemperatureNotification(Notification temperatureNotification) {
        this.temperatureNotification = temperatureNotification;
    }

    public Notification getHumidityNotification() {
        return humidityNotification;
    }

    public void setHumidityNotification(Notification humidityNotification) {
        this.humidityNotification = humidityNotification;
    }

    public Notification getLightNotification() {
        return lightNotification;
    }

    public void setLightNotification(Notification lightNotification) {
        this.lightNotification = lightNotification;
    }

    public boolean isChanges() {
        return changes;
    }

    public void setChanges(boolean changes) {
        this.changes = changes;
    }

    public String getMoistureThreshold() {
        return moistureThreshold;
    }

    public void setMoistureThreshold(String moistureThreshold) {
        this.moistureThreshold = moistureThreshold;
    }

    public String getPumpRunTime() {
        return pumpRunTime;
    }

    public void setPumpRunTime(String pumpRunTime) {
        this.pumpRunTime = pumpRunTime;
    }
}
