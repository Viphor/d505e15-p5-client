package com.d505e15.gps;

import android.location.LocationListener;
import android.location.LocationManager;

import com.d505e15.GPSTracker;
import com.d505e15.TCPClient;

import java.io.IOException;

/**
 * Created by Klostergaard on 30/10/15.
 */
public class LocationPinger implements Runnable {
    private TCPClient client = null;
    private boolean   stopped = false;

    private LocationManager mlocManager = null;
    private LocationListener mlocListener;

    public LocationPinger(String host, int port) {
        client = new TCPClient(host, port);

        mlocListener = MainActivity.getMainActivity().mlocListener;
        mlocManager  = MainActivity.getMainActivity().mlocManager;
    }

    @Override
    public void run() {
        while (!stopped) {
            try {
                String textToSend;
                //mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);

                if (mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    if (GPSTracker.latitude > 0) {
                        textToSend = "lat:" + GPSTracker.latitude + '\n' +
                                "lon:" + GPSTracker.longitude + '\n' +
                                "s:" + GPSTracker.speedString;
                    } else {
                        textToSend = null;
                    }
                } else {
                    textToSend = null;
                }
                if (textToSend != null) {
                    client.connect();
                    client.writeString(textToSend);
                    client.disconnect();
                    Thread.sleep(5000);
                }
            } catch (IOException | InterruptedException e) {
                // Lazy ass solution
                e.printStackTrace();
            }
        }
    }

    public synchronized void stop() {
        stopped = true;
    }
}
