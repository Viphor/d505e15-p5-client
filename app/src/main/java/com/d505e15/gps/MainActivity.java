package com.d505e15.gps;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;


import com.d505e15.GPSTracker;
import com.d505e15.ShowMapActivity;
import com.d505e15.TCPClient;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;

import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends Activity {
    private static MainActivity mainActivity;
    public  static MainActivity getMainActivity() { return mainActivity; }

    private EditText textToSend;
    private EditText hostField;
    private TextView response;
    private Button sendButton;
    private Button connectButton;
    private Button gpsButton;
    private TextView gpsText;
    private Button speedButton;
    private TextView speedText;
    private Button showMap;
    private Button getRoute;
    private RelativeLayout loadingSymbol;
    private TCPConnectionHandler connectionHandler;
    private boolean connected = false;

    public LocationManager mlocManager = null;
    public LocationListener mlocListener;
    public ArrayList<String> list = new ArrayList<String>();
    private Thread autoSendThread = null;
    private MapView mapView;
    private MapController mapController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainActivity    = this;

        getRoute        = (Button)          findViewById(R.id.getRoute);
        textToSend      = (EditText)        findViewById(R.id.text_to_send);
        hostField       = (EditText)        findViewById(R.id.host_field);
        response        = (TextView)        findViewById(R.id.response);
        sendButton      = (Button)          findViewById(R.id.send);
        connectButton   = (Button)          findViewById(R.id.connect_button);
        gpsButton       = (Button)          findViewById(R.id.getGpsButton) ;
        gpsText         = (TextView)        findViewById(R.id.showGPSLocation);
        speedButton     = (Button)          findViewById(R.id.getSpeedButton);
        speedText       = (TextView)        findViewById(R.id.showSpeed);
        showMap         = (Button)          findViewById(R.id.showMap);
        loadingSymbol   = (RelativeLayout)  findViewById(R.id.loadingPanel);
        sendButton.setEnabled(false);
        loadingSymbol.setVisibility(View.GONE);

        mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new GPSTracker();

        getRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingSymbol.setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        connectionHandler.writeString("getRoute,822458514,1814461877");//836995367");//606040601");
                        String output = connectionHandler.readString();

                        if (output == null) {
                            System.err.println("Error in reading string");
                        }

                        System.err.println("Received string: " + output);
                        if (output != null) {
                            String[] outList = output.split(",");

                            for (int i = 0; i < outList.length / 3; i++) {
                                list.add(outList[(i * 3) + 1]);
                                list.add(outList[(i * 3) + 2]);
                            }
                            Intent i = new Intent(MainActivity.this, ShowMapActivity.class);
                            i.putStringArrayListExtra("string_array", list);
                            startActivity(i);
                        } else {
                            Toast t = Toast.makeText(getMainActivity(), "Could not get a route", Toast.LENGTH_LONG);
                            t.setText("Could not get a route");
                            t.show();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loadingSymbol.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                }).start();
            }
        });

        speedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speedText.setText(GPSTracker.speedString + " km/h");

            }
        });

        showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ShowMapActivity.class));

            }
        });
        gpsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);

                if (mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    if (GPSTracker.latitude > 0) {
                        gpsText.setText("Latitude:- " + GPSTracker.latitude + '\n' +
                                "Longitude:- " + GPSTracker.longitude + '\n');
                    } else {
                        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.getMainActivity());
                        alert.setTitle("Wait");
                        alert.setMessage("GPS in progress, please wait.");
                        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        alert.setCancelable(true);
                        alert.show();
                    }
                } else {
                    gpsText.setText("GPS is not turned on...");
                }

            }
        });
    }

    public void autoSend(View view) {
        if (connectionHandler != null && connectionHandler.isConnected()) {
            if (autoSendThread == null) {
                autoSendThread = new Thread(new LocationPinger(hostField.getText().toString(), 1337));
                autoSendThread.start();
            } else {
                Toast t = Toast.makeText(this, "Auto send location is already running", Toast.LENGTH_LONG);
                t.show();
            }
        }
            else
            {
                AlertDialog alerdialog = new AlertDialog.Builder(this).create();
                alerdialog.setTitle("Not connected to server");
                alerdialog.setMessage("The client is not connected to a server");
                alerdialog.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alerdialog.show();
            }

    }

    public void sendMessage(View view) {
       //connectionHandler.writeString(textToSend.getText().toString());
        if (connectionHandler != null && connectionHandler.isConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    connectionHandler.writeString(textToSend.getText().toString());
                    Log.d("ReturnTextTest", connectionHandler.readString());
                }
            }).start();
        } else {
            setResponse("Error");
        }

    }

    public void testTCP(View view) {
        if (connectionHandler != null && connectionHandler.isConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int e = 0;
                    String s = "";
                    for (int i = 0; i < 20000; i++) {
                        s = s + "t";
                        connectionHandler.writeString(s);
                        String t = connectionHandler.readString();
                        if (!t.equals(s)) {
                            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ARGH !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                            e++;
                        }
                    }
                    Toast.makeText(getMainActivity(), "Number of errors: " + e, Toast.LENGTH_LONG).show();
                }
            }).start();
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
        private String response;
        private String textToSend;

        private boolean sendText = false;
        private boolean receiveText = false;

        @Override
        protected Object doInBackground(Object[] params) {
            hostName = hostField.getText().toString();
            client = new TCPClient(hostName, 1337);
            try {
                localSetConnected(true);
                setButtonEnabled(sendButton, true);
                client.connect();

                while (client.isConnected()) {
                    synchronized (this) {
                        this.wait();
                    }

                    if (receiveText) {
                        synchronized (response) {
                            response = client.readString();
                        }
                        receiveText = false;
                    }
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
            synchronized (this) {
                this.notify();
            }
        }

        public synchronized String readString() {
            try {
                return client.readString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
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
