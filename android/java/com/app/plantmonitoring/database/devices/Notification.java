package com.app.plantmonitoring.database.devices;

public class Notification {
    private boolean enabled;
    private String min;
    private String max;

    public Notification(){}

    public Notification(boolean enabled, String min, String max) {
        this.enabled = enabled;
        this.min = min;
        this.max = max;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }
}
