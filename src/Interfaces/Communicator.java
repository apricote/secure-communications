package Interfaces;

import Exceptions.FailedKeyGenerationException;
import Implementations.Message;

public interface Communicator {
    void sendRawMessage(Communicator recv, String msg, boolean isText);

    void sendSecureMessage(Communicator recv, String msg) throws FailedKeyGenerationException;

    void receiveMessage(Message msg);
}
