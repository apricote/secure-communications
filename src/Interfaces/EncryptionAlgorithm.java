package Interfaces;

public abstract class EncryptionAlgorithm {
    public abstract byte[] encrypt(byte[] msg, byte[] key);

    public abstract byte[] decrypt(byte[] cyphertext, byte[] key);
}
