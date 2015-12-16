package com.d505e15;

import android.app.DownloadManager;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.Timer;
import java.util.concurrent.TimeoutException;
import java.util.logging.Handler;

import static java.util.concurrent.TimeUnit.*;


/**
 * Created by Klostergaard on 30/09/15.
 */
public class TCPClient {
    private static final String  DEBUG_FLAG = "TCPClient";
    // Just because..
    private static void log(String text) { System.err.println(DEBUG_FLAG + ": " + text); }

    private static final byte    EOF = 0b0;
    private static final int     BUFFER_SIZE = 512;

    private Socket               socket;
    private String               host;
    private int                  port;

    private boolean              connected = false;

    private DataOutputStream     output;
    private BufferedInputStream  input;

    private static int           clientId = 0;
    private short                lastRequestId = 0;
    ArrayList<String> newlist = new ArrayList<String>();

    private short getNextRequestId() {
        return ++lastRequestId;
    }

    private static final int        requestRetries = 3;
    private static final int        requestTimeout = 5;

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
            socket = new Socket(host, port);
            output = new DataOutputStream(socket.getOutputStream());
            input = new BufferedInputStream(socket.getInputStream());

            writeHeader(new RequestHeader(clientId, getNextRequestId(),
                    clientId == 0 ? RequestCommand.REQUEST_ID : RequestCommand.RETURN_ID,
                    (byte) 0));

            RequestHeader header = handleHeader();
            if (header != null) {
                if (clientId == 0) {
                    if (header.getRequestCommand() == RequestCommand.RETURN_ID) {
                        clientId = header.getId();
                        writeHeader(new RequestHeader(clientId, lastRequestId,
                                RequestCommand.ACK, (byte) 0));
                    } else {
                        throw new IOException("Wrong RequestCommand");
                    }
                } else {
                    if (header.getRequestCommand() != RequestCommand.ACK) {
                        throw new IOException("Wrong RequestCommand");
                    }
                }
            } else {
                throw new IOException("Header could not be read");
            }

            connected = true;
        } catch (IOException e) {
            socket = null;
            output = null;
            input = null;
            connected =  false;
            throw new IOException("Could not connect to server: " + host + ":" + port, e);
        }
    }

    /**
     * Sends a String to the server
     *
     * Needs to be fixed to send in buffer sized byte arrays
     *
     * @param out text to send
     * @throws IOException On no connection, or unable to send
     */
    public final void writeString(String out) throws IOException {
        byte[] bytes = out.getBytes();
        int ADJUSTED_BUFFER_SIZE = BUFFER_SIZE - 1;
        int numOfMessages = (bytes.length / ADJUSTED_BUFFER_SIZE);

        if (bytes.length > ADJUSTED_BUFFER_SIZE) {
            for (int i = 0; i < numOfMessages; i++) {
                writeHeader(new RequestHeader(clientId, getNextRequestId(),
                        RequestCommand.SEND_DATA, false,
                        (byte)(i + 1)));
                output.write(bytes, i * ADJUSTED_BUFFER_SIZE, ADJUSTED_BUFFER_SIZE);
                output.flush();
                writeEOF();
                RequestHeader header = handleHeader();
                if (header == null || header.getRequestCommand() != RequestCommand.ACK) {
                    throw new IOException("Failed to send message: " + lastRequestId);
                }
            }
            writeHeader(new RequestHeader(clientId, getNextRequestId(), RequestCommand.SEND_DATA, true, (byte) (numOfMessages)));
            output.write(bytes, numOfMessages * ADJUSTED_BUFFER_SIZE, bytes.length - (numOfMessages * ADJUSTED_BUFFER_SIZE));
            output.flush();
            writeEOF();
            RequestHeader header = handleHeader();
            if (header == null || header.getRequestCommand() != RequestCommand.ACK) {
                throw new IOException("Failed to send message: " + lastRequestId);
            }
        } else {
            writeHeader(new RequestHeader(clientId, getNextRequestId(),
                    RequestCommand.SEND_DATA, (byte) 0));
            output.write(bytes);
            writeEOF();
            RequestHeader header = handleHeader();
            if (header == null || header.getRequestCommand() != RequestCommand.ACK) {
                throw new IOException("Failed to send message");
            }
        }
    }

    private void writeHeader(RequestHeader header) throws IOException {
        output.write(header.toBytes());
    }

    private void writeEOF() throws IOException {

        output.write(EOF);
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

        StringBuilder stringRead = new StringBuilder();
        RequestHeader prevHeader = null;

        try {
            RequestHeader header = null;
            boolean done = false;

            while (!done) {
                prevHeader = header;
                header = handleHeader();
                if (header != null && header.getRequestCommand() == RequestCommand.SEND_DATA) {
                    stringRead.append(localReadString());
                    writeHeader(new RequestHeader(clientId, lastRequestId,
                            RequestCommand.ACK, header.getMessageNo()));
                    if (header.isLastMessage()) {
                        done = true;
                    }
                } else if (header != null) {
                    writeHeader(new RequestHeader(clientId, lastRequestId,
                            RequestCommand.ERROR, header.getMessageNo()));
                    log(header.getRequestCommand().toString());
                    throw new IOException("Wrong RequestCommand");
                } else {
                    System.err.println("Previous header: " + prevHeader);
                    throw new IOException("Could not read header");
                }
            }
            prevHeader = header;
        } catch (IOException e) {
            throw new IOException("Could not read from server", e);
        }

        System.err.println(prevHeader != null ? prevHeader : "null");

        return stringRead.toString();
    }

    private String localReadString() throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int charsRead = 0;
        while ((charsRead = input.read(buffer)) != -1) {
            if (buffer[charsRead - 1] == EOF) {
                break;
            }
        }

        //Log.d(DEBUG_FLAG, String.valueOf(buffer[0]));

        return charsRead != -1
                ? new String(buffer).substring(0, charsRead - 1)
                : "\n";
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
            writeHeader(new RequestHeader(clientId, getNextRequestId(),
                    RequestCommand.CLOSE_CONNECTION, (byte) 0));

            RequestHeader header = handleHeader();

            if (header != null && header.getRequestCommand() == RequestCommand.CLOSE_CONNECTION) {
                socket.close();
            } else {
                throw new IOException("Error in close request");
            }
        } catch (IOException e) {
            throw new IOException("Could not close connection correctly.", e);
        } finally {
            socket = null;
            output = null;
            input = null;
            connected = false;
        }
    }

    /**
     * TODO fix this comment, and exception handling
     *
     * @return the request header
     */
    private RequestHeader handleHeader() {
        byte[] header = new byte[8];

        try {
            if (input.read(header, 0, 8) < 8) {
                throw new IOException("Could not read the whole header");
            }
        } catch (IOException e) {
            // Lazy ass solution
            e.printStackTrace();
            return null;
        }

        RequestHeader ret = new RequestHeader(header);

        // Might need to be commented
        lastRequestId = ret.getRequestId();

        return ret;
    }

    public void AddToList(String sentMessage)
    {

        if(!newlist.contains(sentMessage)){
            newlist.add(sentMessage);
        }

    }

    public void RemoveFromList( int clientId, short lastRequestId, String sentMessage) {
        if (this.clientId == clientId && this.lastRequestId == lastRequestId) {
            newlist.remove(sentMessage);
        }
    }
}
