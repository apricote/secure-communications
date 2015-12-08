package QueueHandler;

import Implementations.Message;
import Interfaces.Communicator;
import Interfaces.EncryptionAlgorithm;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
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

    private Map<Communicator, KeyExchangeDetails> detailsMap = new HashMap<>();

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
    public void run() {
        while(true) {
            try {
                Message msg = keyGenerationMessageQueue.take();

                byte content = msg.msg[0];

                if(!detailsMap.containsKey(msg.sender)) {
                    detailsMap.put(msg.sender, new KeyExchangeDetails());
                }

                KeyExchangeDetails ked = detailsMap.get(msg.sender);

                switch(content) {
                    case 1: // Lass uns einen Schlüssel erstellen
                        // BOB
                        //Übermittle p
                        ked.p = p;
                        Message transmitPMessage = buildBigIntegerTransmitMessage((byte) 2, ked.p, msg.sender);

                        msg.sender.receiveMessage(transmitPMessage);

                        // Übermittle g
                        ked.g = g;
                        Message transmitGMessage = buildBigIntegerTransmitMessage((byte) 3, ked.g, msg.sender);

                        msg.sender.receiveMessage(transmitGMessage);

                        // Erzeuge Zufallszahl
                        ked.secretNumber = getRandomBigInteger(p);

                        // Berechne Alpha und übertrage
                        ked.alpha = oneWayFunction(ked.g, ked.p, ked.secretNumber);
                        Message transmitAlphaMessage = buildBigIntegerTransmitMessage((byte) 4, ked.alpha, msg.sender);

                        msg.sender.receiveMessage(transmitAlphaMessage);

                        break;

                    case 2: // Empfange p und generiere eigene Geheimzahl
                        // ALICE
                        byte[] pAsBytes = Arrays.copyOfRange(msg.msg, 1, msg.msg.length); // Order Byte weglassen
                        BigInteger p = new BigInteger(pAsBytes);
                        ked.p = p;

                        ked.secretNumber = getRandomBigInteger(p);

                        break;

                    case 3: // Empfange g
                        // ALICE
                        byte[] gAsBytes = Arrays.copyOfRange(msg.msg, 1, msg.msg.length); // Order Byte weglassen
                        BigInteger g = new BigInteger(gAsBytes);
                        ked.g = g;

                        break;

                    case 4: // Empfange Alpha, berechne Beta, übertrage Beta, berechne den geheimen Schlüssel -> Abschluss
                        // ALICE
                        byte[] alphaAsBytes = Arrays.copyOfRange(msg.msg, 1, msg.msg.length); // Order Byte weglassen
                        BigInteger alpha = new BigInteger(alphaAsBytes);
                        ked.alpha = alpha;

                        // berechne+übermittle beta
                        ked.beta = oneWayFunction(ked.g, ked.p, ked.secretNumber);
                        Message transmitBetaMessage = buildBigIntegerTransmitMessage((byte) 5, ked.beta, msg.sender);

                        msg.sender.receiveMessage(transmitBetaMessage);

                        // berechne den geheimen Schlüssel

                        ked.secretKey = oneWayFunction(ked.alpha, ked.p, ked.secretNumber);
                        keys.put(msg.sender, ked.secretKey.toByteArray());

                        //Aufräumen!
                        detailsMap.remove(msg.sender);

                        break;

                    case 5: // Empfange Beta, berechne den geheimen Schlüssel -> Abschluss
                        // BOB
                        byte[] betaAsBytes = Arrays.copyOfRange(msg.msg, 1, msg.msg.length); // Order Byte weglassen
                        BigInteger beta = new BigInteger(betaAsBytes);
                        ked.beta = beta;

                        // berechne den geheimen Schlüssel

                        ked.secretKey = oneWayFunction(ked.beta, ked.p, ked.secretNumber);
                        keys.put(msg.sender, ked.secretKey.toByteArray());

                        //Aufräumen!
                        detailsMap.remove(msg.sender);

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

    private Message buildBigIntegerTransmitMessage(byte order, BigInteger number, Communicator receiver) {
        byte[] orderArray = {order};
        byte[] orderAndNumber = concat(orderArray, number.toByteArray());

        Message result = new Message(owner,
                receiver,
                orderAndNumber,
                false,
                false);

        return result;
    }

    private BigInteger oneWayFunction(BigInteger generator, BigInteger p, BigInteger secretNumber) {
        return generator.modPow(secretNumber, p); // generator^secretNumber % p
    }
}
