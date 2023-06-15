package com.example.software2.dapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DistanceCalculator {

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
        List<Coordinate> finallatlong;
        Coordinate finall=null;

        for (Coordinate c2 : set1) {
            for (Coordinate c1 : set2) {
                double distance = calculateDistance(c1.getLatitude(), c1.getLongitude(), c2.getLatitude(), c2.getLongitude());
                if (distance < minDistance) {
                    minDistance = distance;
                    finall = c1;
                    //System.out.println(c1.getCity()+"  "+c2.getCity());
                }
            }
        }
        finallatlong = new ArrayList<>();
        finallatlong.add(finall);
        return finallatlong;
    }

//    public static void main(String[] args) {
//        List<Coordinate> set1 = Arrays.asList(
//                new Coordinate(51.5074, -0.1278), // London
//                new Coordinate(48.8566, 2.3522), // Paris
//                new Coordinate(17.876184, 73.969780 )
//        );
//        List<Coordinate> set2 = Arrays.asList(
//                new Coordinate(40.7128, -74.0060), // New York
//                new Coordinate(37.7749, -122.4194), // San Francisco
//                new Coordinate(18.445089, 73.868980)
//        );
//
//       // double minDistance = calculateMinimumDistance(set1, set2);
//        //System.out.println("Minimum distance between sets: " + minDistance + " km");
//    }
}

