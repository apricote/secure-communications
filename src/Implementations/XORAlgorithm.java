package Implementations;

import Interfaces.EncryptionAlgorithm;

public class XORAlgorithm extends EncryptionAlgorithm {
    @Override
    public byte[] encrypt(byte[] msg, byte[] key) {
        if(msg.length == 0 || key.length == 0) {
            throw new IllegalArgumentException("Verschlüsselung fehlgeschlagen: Schlüssel oder Nachricht haben eine Länge von 0.");
        }

        byte[] sameLengthKey = new byte[msg.length];

        for(int i = 0; i < msg.length; i++) {
            sameLengthKey[i] = key[i % key.length]; // Falls key.length < msg.length:
                                                    //  Key solange wiederholen bis sie die gleiche Länge haben
                                                    // Falls key.length = msg.length:
                                                    //  Key 1:1 übernehmen
                                                    // Falls key.length > msg.length:
                                                    //  Key begrenzen auf msg.length
        }

        byte[] encryptedBytes = new byte[msg.length];

        // Folgendes wurde aus einer StackOverflow Antwort abgeleitet.
        // Quelle: http://stackoverflow.com/a/18363192
        for(int i = 0; i < msg.length; i++) {
            int msgPart = (int) msg[i];
            int keyPart = (int) sameLengthKey[i];

            int xor = msgPart ^ keyPart;

            encryptedBytes[i] = (byte) (0xff & xor);
        }

        return encryptedBytes;
    }

    public byte[] decrypt(byte[] cyphertext, byte[] key) {
        return encrypt(cyphertext, key); // Die Illusion einer Wahl
    }
}
