package Implementations;

import Interfaces.Communicator;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class Message {
    public final Communicator sender;
    public final Communicator receiver;
    public final boolean isText;
    public final boolean isEncrypted;
    public final byte[] msg;

    public Message(Communicator sender, Communicator receiver, byte[] msg, boolean isEncrypted, boolean isText) {
        this.sender = sender;
        this.receiver = receiver;
        this.msg = msg;
        this.isEncrypted = isEncrypted;
        this.isText = isText;
    }

    public String toString() {
        String stringRepresentation;
        stringRepresentation = "Sender: " + sender.toString() + ", ";
        stringRepresentation += "Recv: " + receiver.toString() + ", ";

        if(isText) {
            stringRepresentation += "Msg: " + new String(msg, StandardCharsets.UTF_8);
        } else {
            stringRepresentation += "Order: " + msg[0] + ", ";
            if(msg[0] != (byte) 1) {
                stringRepresentation += "Msg: " + new BigInteger(Arrays.copyOfRange(msg, 1, msg.length));
            }
        }

        return stringRepresentation;
    }
}
