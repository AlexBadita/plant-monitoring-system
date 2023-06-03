package com.app.plantmonitoring.database.devices;

public class Delay {
    private String value;
    private String unit;

    public Delay(){}

    public Delay(String value, String unit) {
        this.value = value;
        this.unit = unit;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
