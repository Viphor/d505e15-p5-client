package com.d505e15.gps;

import android.app.Activity;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;


import com.d505e15.GPSTracker;
import com.d505e15.TCPClient;

import java.io.IOException;


public class MainActivity extends Activity {
    private EditText textToSend;
    private EditText hostField;
    private TextView response;
    private Button sendButton;
    private Button connectButton;
    private Button gpsButton;
    private TextView gpsText;
    private TCPConnectionHandler connectionHandler;
    private boolean connected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textToSend      = (EditText) findViewById(R.id.text_to_send);
        hostField       = (EditText) findViewById(R.id.host_field);
        response        = (TextView) findViewById(R.id.response);
        sendButton      = (Button)   findViewById(R.id.send);
        connectButton   = (Button)   findViewById(R.id.connect_button);
        gpsButton       = (Button)   findViewById(R.id.getGpsButton) ;
        gpsText         = (TextView) findViewById(R.id.showGPSLocation);

        sendButton.setEnabled(false);

        Button ASButton = (Button) findViewById(R.id.getGpsButton);

        ASButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LocationManager mlocManager=null;
                LocationListener mlocListener;
                mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                mlocListener = new GPSTracker();
                mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, mlocListener);

                if (mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    if(GPSTracker.latitude>0)
                    {
                        gpsText.append("Latitude:- " + GPSTracker.latitude + '\n');
                        gpsText.append("Longitude:- " + GPSTracker.longitude + '\n');
                    }
                  /*  else
                    {
                        alert.setTitle("Wait");
                        alert.setMessage("GPS in progress, please wait.");
                        alert.setPositiveButton("OK", null);
                        alert.show();
                    }*/
                } else {
                    gpsText.setText("GPS is not turned on...");
                }

            }
        });
    }

    public void sendMessage(View view) {
        //connectionHandler.writeString(textToSend.getText().toString());
        if (connectionHandler != null && connectionHandler.isConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    connectionHandler.writeString(textToSend.getText().toString());
                }
            }).start();
        } else {
            setResponse("Error");
        }
    }

    @SuppressWarnings("unchecked")
    public void connect(View view) {
        if (!connected) {
            connectionHandler = new TCPConnectionHandler();
            connectionHandler.execute();
        } else if (connectionHandler != null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    connectionHandler.disconnect();
                }
            }).start();
        }
    }

    private synchronized void setResponse(String s) {
        response.setText(s);
    }

    private synchronized void setConnected(boolean b) {
        connected = b;

        if (connected) {
            connectButton.setText("Disconnect");
        } else {
            connectButton.setText("Connect");
        }
    }

    /**
     * Class that handles the connection asynchronously
     *
     * To initiate the run loop, call execute()
     * Once running, call writeString(String output) to send
     * the string to the server
     */
    private class TCPConnectionHandler extends AsyncTask {
        private TCPClient client;
        private String hostName;

        @Override
        protected Object doInBackground(Object[] params) {
            hostName = hostField.getText().toString();
            client = new TCPClient(hostName, 1337);
            try {
                localSetConnected(true);
                setButtonEnabled(sendButton, true);
                client.connect();

                while (client.isConnected()) {
                    Thread.sleep(100, 0);
                    //String response = client.readString();
                    //Log.d("TCPResponse", response);
                    //localSetResponse(response);
                }
            } catch (IOException e) {
                // Lazy ass solution
                e.printStackTrace();
                if (!client.isConnected()) {
                    localSetResponse("Could not connect");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                localSetConnected(false);
                setButtonEnabled(sendButton, false);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            connectionHandler = null;
        }

        public synchronized void writeString(String output) {
            try {
                client.writeString(output);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public synchronized boolean isConnected() {
            return client.isConnected();
        }

        public synchronized void disconnect() {
            if (isConnected()) {
                try {
                    client.disconnect();
                } catch (IOException e) {
                    localSetResponse("Disconnect failed");
                }
            }
        }

        private synchronized void localSetResponse(final String s) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setResponse(s);
                }
            });
        }

        private synchronized void localSetConnected(final boolean b) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setConnected(b);
                }
            });
        }

        private synchronized void setButtonEnabled(final Button button, final boolean b) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    button.setEnabled(b);
                }
            });
        }

    }






}
