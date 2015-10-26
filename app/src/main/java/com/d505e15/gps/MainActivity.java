package com.d505e15.gps;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.d505e15.TCPClient;

import java.io.IOException;


public class MainActivity extends Activity {
    private EditText textToSend;
    private EditText hostField;
    private TextView response;
    private Button sendButton;
    private Button connectButton;
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

        sendButton.setEnabled(false);
    }

    public void sendMessage(View view) {
        if (connectionHandler != null && connectionHandler.isConnected()) {
            connectionHandler.writeString(textToSend.getText().toString());
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
            connectionHandler.disconnect();
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
                    String response = client.readString();
                    Log.d("TCPResponse", response);
                    localSetResponse(response);
                }
            } catch (IOException e) {
                // Lazy ass solution
                e.printStackTrace();
                if (!client.isConnected()) {
                    localSetResponse("Could not connect");
                }
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

        public void writeString(String output) {
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
