package QueueHandler;

import Implementations.Message;
import Interfaces.Communicator;
import Interfaces.EncryptionAlgorithm;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class UserMessageQueueHandler extends Thread {
    BlockingQueue<Message> userMessageQueue;
    EncryptionAlgorithm encryptionAlgorithm;
    Map<Communicator, byte[]> encryptionKeys;

    public UserMessageQueueHandler(BlockingQueue<Message> umq,
                                   EncryptionAlgorithm ea,
                                   Map<Communicator, byte[]> keys) {
        userMessageQueue = umq;
        encryptionAlgorithm = ea;
        encryptionKeys = keys;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Message msg = userMessageQueue.take();
                byte[] unencryptedBytes;
                if (msg.isEncrypted && !encryptionKeys.containsKey(msg.sender)) {
                    Thread.sleep(10);
                }
                if (msg.isEncrypted && encryptionKeys.containsKey(msg.sender)) {
                    unencryptedBytes = encryptionAlgorithm.decrypt(msg.msg, encryptionKeys.get(msg.sender));
                } else {
                    unencryptedBytes = msg.msg;
                }
                String text = new String(unencryptedBytes, StandardCharsets.UTF_8);

                System.out.println(text);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
