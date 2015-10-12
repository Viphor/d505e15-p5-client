package com.d505e15.gps;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by Klostergaard on 30/09/15.
 */
public class TCPClient {
    private static final String DEBUG_FLAG = "TCPClient";

    private static final int BUFFER_SIZE = 65536;
    private Socket socket;
    private String host;
    private int port;

    private boolean connected = false;

    private DataOutputStream outToServer;
    private BufferedReader inFromServer;

    //private int sessionId = 0;

    /**
     * Initializes the TCPClient with a host name and a server port
     * @param host The name of the host
     * @param port The server port
     */
    public TCPClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Tells whether or not the client is connected to the server
     * @return true if connected, and false if not
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Sets up a connection to the server
     * @throws IOException If no connection to the server
     */

    public void connect() throws IOException {
        try {
            //Log.d(DEBUG_FLAG, "connecting");
            socket = new Socket(host, port);
            //Log.d(DEBUG_FLAG, "connected");
            outToServer = new DataOutputStream(socket.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;
        } catch (IOException e) {
            socket = null;
            outToServer = null;
            inFromServer = null;
            connected =  false;
            throw new IOException("Could not connect to server: " + host + ":" + port, e);
        }
    }

    /**
     * Sends a single line to the server
     * @param input text to send
     * @throws IOException On no connection, or unable to send
     */
    public void writeString(String input) throws IOException {
        if (!connected) {
            throw new IOException("Not connected! Tip: call connect() first!");
        }
        String toSend;
        // Encoding the bytes of the sessionId to chars
        //char high = (char)(sessionId >> 16);
        //char low = (char)(sessionId & 0xffff);
        // String.valueOf() is for ensuring that we create a string,
        // and not just adding the values of high and low.
        toSend = /*String.valueOf(high) + low +*/ input + "\u001a"; // \u001a = EOF
        byte[] bytes = toSend.getBytes();

        outToServer.write(bytes);
    }

    /**
     * Reads a single line from the server.
     * Does not return if EOF char is not met.
     *
     * @return The String received from the server, empty on system commands
     * @throws IOException On no connection, or unable to read
     */
    public String readString() throws IOException {
        if (!connected) {
            throw new IOException("Not connected! Tip: call connect() first!");
        }

        String stringRead = null;
        try {
            // If sessionId turns out to be needed, implement here
            stringRead = localReadString();
            //System.out.println("Read: " + stringRead);
        } catch (IOException e) {
            throw new IOException("Could not read from server", e);
        }

        if (stringRead.equals("close")) {
            disconnect();
            return ""; // No need to return internal commands
        }

        return stringRead;
    }

    private String localReadString() throws IOException {
        char[] buffer = new char[TCPClient.BUFFER_SIZE];
        int charsRead = 0;
        while ((charsRead = inFromServer.read(buffer)) != -1) {
            if (buffer[charsRead - 1] == '\u001a') {
                break;
            }
        }

        //Log.d(DEBUG_FLAG, String.valueOf(buffer[0]));

        return charsRead != -1
                ? new String(buffer).substring(0, charsRead - 1)
                : "\n"; // \u001a = EOF
    }

    /**
     * Disconnects from the server,
     * and sends a message to the server to close the connection server-side
     *
     * @throws IOException
     */
    public void disconnect() throws IOException {
        if (!connected) {
            throw new IOException("Not connected! Tip: call connect() first!");
        }

        try {
            // Needs to be changed to some single byte instruction
            outToServer.writeBytes("close\u001a");
            socket.close();
        } catch (IOException e) {
            throw new IOException("Could not close connection correctly.", e);
        } finally {
            socket = null;
            outToServer = null;
            inFromServer = null;
            connected = false;
        }
    }
}
