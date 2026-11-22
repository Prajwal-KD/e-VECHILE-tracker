package com.v2v.smartsystem.utils;

/**
 * Geographic and spatial utility methods for V2V radar and collision warning calculations.
 */
public class DistanceCalculator {

    private static final double EARTH_RADIUS_METERS = 6371000.0;

    /**
     * Calculates the great-circle distance between two geographic coordinates using the Haversine formula.
     * @return Distance in meters.
     */
    public static double calculateDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        if ((lat1 == 0.0 && lon1 == 0.0) || (lat2 == 0.0 && lon2 == 0.0)) {
            return 9999.0; // Fallback if GPS not yet locked
        }

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }

    /**
     * Calculates the initial bearing from point 1 to point 2 in degrees (0° to 360°).
     */
    public static double calculateBearingDegrees(double lat1, double lon1, double lat2, double lon2) {
        double dLon = Math.toRadians(lon2 - lon1);
        double y = Math.sin(dLon) * Math.cos(Math.toRadians(lat2));
        double x = Math.cos(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) -
                Math.sin(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(dLon);

        double brng = Math.toDegrees(Math.atan2(y, x));
        return (brng + 360.0) % 360.0;
    }

    /**
     * Calculates relative bearing with respect to the host vehicle's heading.
     * @return Angle between -180° and +180° (0° = straight ahead, +90° = right, -90° = left).
     */
    public static double calculateRelativeBearing(double hostHeading, double absoluteBearing) {
        double rel = absoluteBearing - hostHeading;
        while (rel > 180) rel -= 360;
        while (rel < -180) rel += 360;
        return rel;
    }

    /**
     * Calculates Time-To-Collision (TTC) in seconds.
     * @param distanceMeters Distance to target vehicle.
     * @param relativeSpeedMps Speed difference (closing speed in m/s).
     * @return Estimated TTC in seconds, or Double.MAX_VALUE if not closing.
     */
    public static double calculateTimeToCollision(double distanceMeters, double relativeSpeedMps) {
        if (relativeSpeedMps <= 0.1) {
            return Double.MAX_VALUE; // Not closing in
        }
        return distanceMeters / relativeSpeedMps;
    }
}
