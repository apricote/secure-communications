package QueueHandler;

import Implementations.Message;
import Interfaces.Communicator;
import Interfaces.EncryptionAlgorithm;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class KeyGenerationMessageQueueHandler extends Thread {

    private BlockingQueue<Message> keyGenerationMessageQueue;
    private EncryptionAlgorithm ea;
    private Map<Communicator, byte[]> keys;

    private Communicator owner;

    private BigInteger p;
    private BigInteger g;

    private Map<Communicator, KeyExchangeDetails> detailsMap;

    public KeyGenerationMessageQueueHandler(BlockingQueue<Message> keyGenerationMessageQueue,
                                            EncryptionAlgorithm ea,
                                            Map<Communicator, byte[]> keys,
                                            BigInteger p,
                                            BigInteger g,
                                            Communicator owner) {
        this.keyGenerationMessageQueue = keyGenerationMessageQueue;
        this.ea = ea;
        this.keys = keys;
        this.p = p;
        this.g = g;
        this.owner = owner;
    }

    @Override
    public void run() { // TODO Setup KeyExchangeDetails for initiator
        while(true) {
            try {
                Message msg = keyGenerationMessageQueue.take();

                byte content = msg.msg[0];

                switch(content) {
                    case 1: // Lass uns einen Schlüssel erstellen
                        detailsMap.put(msg.sender, new KeyExchangeDetails());

                        byte[] orderTwo = {(byte) 2};
                        byte[] orderAndP = concat(orderTwo, p.toByteArray());
                        Message transmitPMessage = new Message(owner,
                                msg.sender,
                                orderAndP,
                                false,
                                false);

                        msg.sender.receiveMessage(transmitPMessage);


                        byte[] orderThree = {(byte) 3};
                        byte[] orderAndG = concat(orderThree, g.toByteArray());
                        Message transmitGMessage = new Message(owner,
                                msg.sender,
                                orderAndG,
                                false,
                                false);

                        msg.sender.receiveMessage(transmitGMessage);


                        byte[] orderFour = {(byte) 4};


                        break;

                    case 2: // Empfange p und generiere eigene Geheimzahl
                        byte[] pAsBytes = Arrays.copyOfRange(msg.msg, 1, msg.msg.length); // Order Byte weglassen
                        BigInteger p = new BigInteger(pAsBytes);
                        detailsMap.get(msg.sender).p = p;

                        detailsMap.get(msg.sender).secretNumber = getRandomBigInteger();

                        break;

                    case 3: // Empfange g
                        byte[] gAsBytes = Arrays.copyOfRange(msg.msg, 1, msg.msg.length); // Order Byte weglassen
                        BigInteger g = new BigInteger(gAsBytes);
                        detailsMap.get(msg.sender).g = g;

                        break;

                    case 127: // Etwas ist falsch gelaufen!
                        detailsMap.remove(msg.sender);

                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class KeyExchangeDetails {
        public BigInteger secretNumber;
        public BigInteger p;
        public BigInteger g;
        public BigInteger alpha;
        public BigInteger beta;
        public BigInteger secretKey;
    }

    private byte[] concat(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c= new byte[aLen+bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    private BigInteger getRandomBigInteger(BigInteger max) {
        Random rand = new Random();
        BigInteger result;
        do {
            result = new BigInteger(max.bitLength(), rand);
        } while( result.compareTo(max) >= 0 );
        return result;
    }
}
