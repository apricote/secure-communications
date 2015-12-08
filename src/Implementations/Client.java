package Implementations;

import Exceptions.FailedKeyGenerationException;
import Interfaces.Communicator;
import Interfaces.EncryptionAlgorithm;
import QueueHandler.KeyGenerationMessageQueueHandler;
import QueueHandler.MessageQueueHandler;
import QueueHandler.UserMessageQueueHandler;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Client implements Communicator {
    private Map<Communicator, byte[]> encryptionKeys = new HashMap<>();

    private BlockingQueue<Message> generalMessageQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<Message> keyGenerationMessageQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<Message> userMessageQueue = new LinkedBlockingQueue<>();

    private final BigInteger g;
    private final BigInteger p;

    private EncryptionAlgorithm ea;

    public Client() {
        ea = new XORAlgorithm();
        g = new BigInteger("2");
        p = new BigInteger( "FFFFFFFF FFFFFFFF C90FDAA2 2168C234 C4C6628B 80DC1CD1" +
                            "29024E08 8A67CC74 020BBEA6 3B139B22 514A0879 8E3404DD" +
                            "EF9519B3 CD3A431B 302B0A6D F25F1437 4FE1356D 6D51C245" +
                            "E485B576 625E7EC6 F44C42E9 A637ED6B 0BFF5CB6 F406B7ED" +
                            "EE386BFB 5A899FA5 AE9F2411 7C4B1FE6 49286651 ECE45B3D" +
                            "C2007CB8 A163BF05 98DA4836 1C55D39A 69163FA8 FD24CF5F" +
                            "83655D23 DCA3AD96 1C62F356 208552BB 9ED52907 7096966D" +
                            "670C354E 4ABC9804 F1746C08 CA18217C 32905E46 2E36CE3B" +
                            "E39E772C 180E8603 9B2783A2 EC07A28F B5C55DF0 6F4C52C9" +
                            "DE2BCBF6 95581718 3995497C EA956AE5 15D22618 98FA0510" +
                            "15728E5A 8AACAA68 FFFFFFFF FFFFFFFF".replaceAll(" ", ""), 16);
        // p und g wurden nach RFC 3526 gewählt. Sie entsprechen Gruppen ID 14. 2048bit

        MessageQueueHandler mqh = new MessageQueueHandler(generalMessageQueue,
                keyGenerationMessageQueue,
                userMessageQueue);
        mqh.start();

        UserMessageQueueHandler umqh = new UserMessageQueueHandler(userMessageQueue, ea, encryptionKeys);
        umqh.start();

        KeyGenerationMessageQueueHandler kgmqh = new KeyGenerationMessageQueueHandler(keyGenerationMessageQueue, ea, encryptionKeys, p, g, this);
        kgmqh.start();
    }

    @Override
    public void sendRawMessage(Communicator recv, String msg, boolean isText) {
        Message message = new Message(this, recv, msg.getBytes(StandardCharsets.UTF_8), false, isText);
        recv.receiveMessage(message);
    }

    @Override
    public void sendSecureMessage(Communicator recv, String msg) throws FailedKeyGenerationException {
        if(!encryptionKeys.containsKey(recv)) {
            initiateKeyExchange(recv);
        }

        byte[] encryptedMessage = ea.encrypt(msg.getBytes(StandardCharsets.UTF_8), encryptionKeys.get(recv));
        Message message = new Message(this, recv, encryptedMessage, true, true);

        recv.receiveMessage(message);

    }

    @Override
    public void receiveMessage(Message msg) {
        generalMessageQueue.add(msg);
    }

    private void initiateKeyExchange(Communicator bob) {
        byte[] keyExchangeInitiationMsg = {(byte) 1};
        Message initiationMsg = new Message(this, bob, keyExchangeInitiationMsg, false, false);
        bob.receiveMessage(initiationMsg);
    }
}
