package com.example.software2.dapp;

public class Coordinate {
    public double latitude;
    public double longitude;
    public String address;
    public Coordinate(double latitude, double longitude,String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }
    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public String getAddress(){
        return address;
    }
}

