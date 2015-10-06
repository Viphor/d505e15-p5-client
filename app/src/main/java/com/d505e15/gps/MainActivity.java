package com.d505e15.gps;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;


public class MainActivity extends Activity {
    private EditText textToSend;
    private TextView response;
    private Button sendButton;
    private Button connectButton;
    private TCPConnectionHandler connectionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textToSend      = (EditText) findViewById(R.id.text_to_send);
        response        = (TextView) findViewById(R.id.response);
        sendButton      = (Button)   findViewById(R.id.send);
        connectButton   = (Button)   findViewById(R.id.connect_button);

        sendButton.setEnabled(false);
    }

    public void sendMessage(View view) {
        if (connectionHandler != null && connectionHandler.isConnected()) {
            connectionHandler.writeLine(textToSend.getText().toString());
        } else {
            setResponse("Error");
        }
    }

    @SuppressWarnings("unchecked")
    public void connect(View view) {
        connectionHandler = new TCPConnectionHandler();
        connectionHandler.execute();
    }

    private synchronized void setResponse(String s) {
        response.setText(s);
    }

    /**
     * Class that handles the connection asynchronously
     *
     * To initiate the run loop, call execute()
     * Once running, call writeLine(String output) to send
     * the string to the server
     */
    private class TCPConnectionHandler extends AsyncTask {
        private TCPClient client;

        @Override
        protected Object doInBackground(Object[] params) {
            client = new TCPClient("192.168.43.44", 1337);
            try {
                client.connect();
                setButtonEnabled(connectButton, false);
                setButtonEnabled(sendButton, true);

                while (client.isConnected()) {
                    localSetResponse(client.readLine());
                }
            } catch (IOException e) {
                // Lazy ass solution
                e.printStackTrace();
                if (!client.isConnected()) {
                    localSetResponse("Could not connect");
                }
            } finally {
                setButtonEnabled(connectButton, true);
                setButtonEnabled(sendButton, false);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            connectionHandler = null;
        }

        public synchronized void writeLine(String output) {
            try {
                client.writeLine(output);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public synchronized boolean isConnected() {
            return client.isConnected();
        }

        private synchronized void localSetResponse(final String s) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setResponse(s);
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
