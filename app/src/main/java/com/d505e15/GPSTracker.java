package com.d505e15;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Created by Michael on 26-10-2015.
 */
public class GPSTracker implements LocationListener {

    public static double latitude;
    public static double longitude;
    public static float speed;
    public static String speedString;

    @Override
    public void onLocationChanged(Location loc)
    {
        loc.getLatitude();
        loc.getLongitude();
        latitude=loc.getLatitude();
        longitude=loc.getLongitude();
        speed = loc.getSpeed();
        speedString = Float.toString(speed);
    }

    @Override
    public void onProviderDisabled(String provider)
    {
        //print "Currently GPS is Disabled";
    }
    @Override
    public void onProviderEnabled(String provider)
    {
        //print "GPS got Enabled";
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
    }
}
