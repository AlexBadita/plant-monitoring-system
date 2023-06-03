package com.app.plantmonitoring.database;

import com.app.plantmonitoring.database.devices.Device;
import com.app.plantmonitoring.database.devices.History;
import com.app.plantmonitoring.database.plants.Plant;
import com.app.plantmonitoring.database.users.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final DatabaseReference userRef = database.getReference("Users");
    private final DatabaseReference deviceRef = database.getReference("Devices");
    private final DatabaseReference plantRef = database.getReference("Plants");
    private final StorageReference devicesImageRef = storage.getReference("Devices");
    private final StorageReference plantsImageRef = storage.getReference("Plants");
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();

    private User currentUser = new User();
    private List<Device> allDevices = new ArrayList<>();
    private List<String> allDevicesIds = new ArrayList<>();
    private String userKey;
    private Device selectedDevice;
    private String currentDeviceId;
    private List<Plant> allPlants;
    private List<History> currentHistory;

    // Singleton
    private static DatabaseHelper instance;

    private DatabaseHelper() { }

    public static DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        return instance;
    }

    public FirebaseDatabase getDatabase() {
        return database;
    }

    public DatabaseReference getUserRef() {
        return userRef;
    }

    public DatabaseReference getDeviceRef() {
        return deviceRef;
    }

    public DatabaseReference getPlantRef() {
        return plantRef;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseMessaging getFirebaseMessaging() {
        return firebaseMessaging;
    }

    public FirebaseUser getUser(){
        return auth.getCurrentUser();
    }

    public void setUserKey(String key){
        userKey = key;
    }

    public String getUserKey(){
        return userKey;
    }

    public void setCurrentUser(User user){
        currentUser = user;
    }

    public User getCurrentUser(){
        return currentUser;
    }

//    public void setUser() {
//        FirebaseUser user = auth.getCurrentUser();
//        String userId = user.getUid();
//        Query query = userRef.orderByChild("id").equalTo(userId);
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    for (DataSnapshot data : snapshot.getChildren()) {
//                        userKey = data.getKey();
//                        userKey = data.getKey();
//                        currentUser = initUser(data);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//            }
//        });
//    }
//
//    private User initUser(DataSnapshot data) {
//        String id = data.child("id").getValue(String.class);
//        String name = data.child("name").getValue(String.class);
//        String email = data.child("email").getValue(String.class);
//        String password = data.child("password").getValue(String.class);
//        boolean isAdmin = data.child("admin").getValue(Boolean.class);
//        List<String> deviceIds = new ArrayList<>();
//        for (DataSnapshot dataSnapshot : data.child("devices").getChildren()) {
//            deviceIds.add(dataSnapshot.getValue(String.class));
//        }
//
//        return new User(id, name, email, password, isAdmin, deviceIds);
//    }

//    public User getUser() {
//        return currentUser;
//    }

    public List<Device> getAllDevices() {
        return allDevices;
    }

    public void setAllDevices(List<Device> devices){
        allDevices = devices;
    }

    public List<String> getAllDevicesIds() {
        return allDevicesIds;
    }

    public void setAllDevicesIds(List<String> devicesIds){
        allDevicesIds = devicesIds;
    }

    public void addUserDevice(String id, String name) {
        userRef.child(userKey).child("devices").push().setValue(id);
        currentUser.addDevice(id);

//        int index = allDevicesIds.indexOf(id);
        deviceRef.child(id).child("name").setValue(name);
//        allDevices.get(index).setName(name);
    }

    public StorageReference getDevicesImageRef() {
        return devicesImageRef;
    }

    public void setSelectedDevice(Device device){
        selectedDevice = device;
    }

    public Device getSelectedDevice(){
        return selectedDevice;
    }

    public String getCurrentDeviceId() {
        return currentDeviceId;
    }

    public void setCurrentDeviceId(String currentDeviceId) {
        this.currentDeviceId = currentDeviceId;
    }

    public List<Plant> getAllPlants() {
        return allPlants;
    }

    public void setAllPlants(List<Plant> allPlants) {
        this.allPlants = allPlants;
    }

    public List<History> getCurrentHistory() {
        return currentHistory;
    }

    public void setCurrentHistory(List<History> currentHistory) {
        this.currentHistory = currentHistory;
    }
}
