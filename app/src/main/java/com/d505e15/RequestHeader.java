package com.d505e15;

/**
 * Created by Klostergaard on 14/10/15.
 */
public class RequestHeader {
    private int     id          = 0;
    private short   requestId   = 0;
    private byte    command     = 0;
    private boolean lastMessage = true;
    private byte    messageNo   = 0;
    private static final int        requestRetries = 3;
    private static final int        requestTimeout = 5;

    public RequestHeader(int id, short requestId, RequestCommand command, byte messageNo) {
        this.id = id;
        this.requestId = requestId;
        this.command = command.getByte();
        this.lastMessage = true;
        this.messageNo = messageNo;
    }

    public RequestHeader(int id, short requestId, RequestCommand command, boolean lastMessage, byte messageNo) {
        this.id = id;
        this.requestId = requestId;
        this.command = command.getByte();
        this.lastMessage = lastMessage;
        this.messageNo = messageNo;
    }

    public RequestHeader(int id) {
        this.id = id;
        this.requestId = 0;
        this.command = RequestCommand.ERROR.getByte();
        this.messageNo = 0;
    }

    public RequestHeader(byte[] header) {
        if (header.length < 8) {
            throw new ExceptionInInitializerError("header must be at least 8 bytes long! idiot!");
        }
        id = header[0] << 24 | (header[1] & 0xFF) << 16 | (header[2] & 0xFF) << 8 | (header[3] & 0xFF);
        requestId = (short)(header[4] << 8 | (header[5]));
        command = (byte)(header[6] >>> 1);
        lastMessage = (header[6] & 0x01) == 1;
        messageNo = header[7];
    }

    public int getId() {
        return id;
    }

    public short getRequestId() {
        return requestId;
    }

    public byte getCommand() {
        return command;
    }

    public RequestCommand getRequestCommand() {
        return RequestCommand.getRequestCommand(command);
    }

    public boolean isLastMessage() {
        return lastMessage;
    }

    public byte getMessageNo() {
        return messageNo;
    }

    public byte[] toBytes() {
        byte[] bytes = new byte[8];

        bytes[0] = (byte)(id >> 24);
        bytes[1] = (byte)((id >> 16) & 0xFF);
        bytes[2] = (byte)((id >> 8)  & 0xFF);
        bytes[3] = (byte)(id & 0xFF);

        bytes[4] = (byte)(requestId >> 8);
        bytes[5] = (byte)(requestId & 0xFF);

        bytes[6] = (byte)((command << 1) + (lastMessage ? 1 : 0));

        bytes[7] = messageNo;

        return bytes;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RequestHeader)) {
            return false;
        }

        RequestHeader h = (RequestHeader)o;

        return id == h.id && requestId == h.requestId && lastMessage == h.lastMessage;
    }

    @Override
    public String toString() {
        return "Id:" + id + ", request:" + requestId + ":" + messageNo + ", last message:" + lastMessage + ", request command: " + getRequestCommand();
    }
}
