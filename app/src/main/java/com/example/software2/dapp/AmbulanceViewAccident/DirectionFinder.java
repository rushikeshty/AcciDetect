package com.example.software2.dapp.AmbulanceViewAccident;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class DirectionFinder {
    private final String destination;

    public DirectionFinder(String destination) {
        this.destination = destination;
    }

    public String createUrl() throws UnsupportedEncodingException {
        return URLEncoder.encode(destination, "utf-8");
    }
}
