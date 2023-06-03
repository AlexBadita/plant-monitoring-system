package com.app.plantmonitoring.database.users;

import java.util.List;

public class User {
    private String id;
    private String name;
    private String email;
    private String password;
    private List<String> deviceIds;

    public User(){}

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public User(String id, String name, String email, String password, List<String> deviceIds) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.deviceIds = deviceIds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getDevices() {
        return deviceIds;
    }

    public void setDevices(List<String> deviceIds) {
        this.deviceIds = deviceIds;
    }

    public void addDevice(String id){
        this.deviceIds.add(id);
    }
}
