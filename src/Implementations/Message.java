package Implementations;

import Interfaces.Communicator;

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
}
