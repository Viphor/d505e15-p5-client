package com.d505e15;

/**
 * Created by Klostergaard on 14/10/15.
 */
public enum RequestCommand {
    REQUEST_ID {
        @Override
        public byte getByte() {
            return 0x1;
        }
    },
    RETURN_ID { // please change name!

        @Override
        public byte getByte() {
            return 0x2;
        }
    },
    ACK {
        @Override
        public byte getByte() {
            return 0x3;
        }
    },
    SEND_DATA {
        @Override
        public byte getByte() {
            return 0x4;
        }
    },
    CLOSE_CONNECTION {
        @Override
        public byte getByte() {
            return 0xF;
        }
    },
    ERROR {
        @Override
        public byte getByte() {
            return 0x0;
        }
    };

    public abstract byte getByte();

    public static RequestCommand getRequestCommand(byte command) {
        for (RequestCommand c : RequestCommand.values()) {
            if (c.getByte() == command) {
                return c;
            }
        }
        return ERROR;
    }
}
