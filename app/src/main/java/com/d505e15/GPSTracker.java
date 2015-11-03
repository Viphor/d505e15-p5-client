package com.d505e15;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

import com.d505e15.gps.MainActivity;

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
        speedString = Float.toString(speed * 3.6f);
    }

    @Override
    public void onProviderDisabled(String provider)
    {
        //print "Currently GPS is Disabled";
        Toast t = Toast.makeText(MainActivity.getMainActivity(), "Currently GPS is Disabled", Toast.LENGTH_LONG);
        t.show();
    }
    @Override
    public void onProviderEnabled(String provider)
    {
        //print "GPS got Enabled";
        Toast t = Toast.makeText(MainActivity.getMainActivity(), "GPS got Enabled", Toast.LENGTH_LONG);
        t.show();
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
    }
}
