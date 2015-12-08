package Interfaces;

import Exceptions.FailedKeyException;
import Implementations.Message;

public interface Communicator {
    void sendRawMessage(Communicator recv, String msg, boolean isText);

    void sendSecureMessage(Communicator recv, String msg);

    void receiveMessage(Message msg);
}
