package top.wxip.gnsslink;

import android.location.Location;
import android.os.SystemClock;

public class GpsModel {
    private double altitude;
    private float accuracy;
    private float bearing;
    private float bearingAccuracyDegrees;
    private float speed;
    private float speedAccuracyMetersPerSecond;
    private double longitude;
    private double latitude;

    public void fromLoc(Location loc) {
        this.altitude = loc.getAltitude();
        this.accuracy = loc.getAccuracy();
        this.bearing = loc.getBearing();
        this.bearingAccuracyDegrees = loc.getBearingAccuracyDegrees();
        this.speed = loc.getSpeed();
        this.speedAccuracyMetersPerSecond = loc.getSpeedAccuracyMetersPerSecond();
        this.longitude = loc.getLongitude();
        this.latitude = loc.getLatitude();
    }

    public Location toLoc() {
        final Location loc = new Location("gps");
        loc.setAltitude(this.altitude);
        loc.setAccuracy(this.accuracy);
        loc.setBearing(this.bearing);
        loc.setBearingAccuracyDegrees(this.bearingAccuracyDegrees);
        loc.setSpeed(this.speed);
        loc.setSpeedAccuracyMetersPerSecond(this.speedAccuracyMetersPerSecond);
        loc.setLongitude(this.longitude);
        loc.setLatitude(this.latitude);
        loc.setTime(System.currentTimeMillis());
        loc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        return loc;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public float getBearingAccuracyDegrees() {
        return bearingAccuracyDegrees;
    }

    public void setBearingAccuracyDegrees(float bearingAccuracyDegrees) {
        this.bearingAccuracyDegrees = bearingAccuracyDegrees;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getSpeedAccuracyMetersPerSecond() {
        return speedAccuracyMetersPerSecond;
    }

    public void setSpeedAccuracyMetersPerSecond(float speedAccuracyMetersPerSecond) {
        this.speedAccuracyMetersPerSecond = speedAccuracyMetersPerSecond;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
