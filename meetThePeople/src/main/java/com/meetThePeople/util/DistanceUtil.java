package com.meetThePeople.util;

import org.springframework.stereotype.Component;

@Component
public class DistanceUtil {
    
    private static final double EARTH_RADIUS = 6371; // Earth's radius in kilometers
    
    /**
     * Calculate distance between two points using Haversine formula
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in kilometers
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }
    
    /**
     * Check if two points are within specified distance
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @param maxDistance Maximum distance in kilometers
     * @return true if within distance, false otherwise
     */
    public boolean isWithinDistance(double lat1, double lon1, double lat2, double lon2, double maxDistance) {
        double distance = calculateDistance(lat1, lon1, lat2, lon2);
        return distance <= maxDistance;
    }
} 