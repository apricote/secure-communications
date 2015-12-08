package Implementations;

import Exceptions.FailedKeyException;
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
        p = new BigInteger( "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" +
                            "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" +
                            "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" +
                            "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED" +
                            "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D" +
                            "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F" +
                            "83655D23DCA3AD961C62F356208552BB9ED529077096966D" +
                            "670C354E4ABC9804F1746C08CA18217C32905E462E36CE3B" +
                            "E39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9" +
                            "DE2BCBF6955817183995497CEA956AE515D2261898FA0510" +
                            "15728E5A8AACAA68FFFFFFFFFFFFFFFF", 16);
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
    public void sendSecureMessage(Communicator recv, String msg) {
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

        while(!encryptionKeys.containsKey(bob)) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
