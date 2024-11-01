package com.example.software2.dapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DistanceCalculatorAlgorithm {

    private static final double EARTH_RADIUS = 6371; // in kilometers

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS * c;
        return distance;
    }

    public static List<Coordinate> calculateMinimumDistance(List<Coordinate> set1, List<Coordinate> set2) {
        double minDistance = Double.POSITIVE_INFINITY;
        List<Coordinate> fastestRoute;
        Coordinate finall=null;

        for (Coordinate c2 : set1) {
            for (Coordinate c1 : set2) {
                double distance = calculateDistance(c1.getLatitude(), c1.getLongitude(), c2.getLatitude(), c2.getLongitude());
                if (distance < minDistance) {
                    minDistance = distance;
                    finall = c1;
                 }
            }
        }
        fastestRoute = new ArrayList<>();
        fastestRoute.add(finall);
        return fastestRoute;
    }


}

