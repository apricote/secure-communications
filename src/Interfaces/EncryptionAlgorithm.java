package Interfaces;

public abstract class EncryptionAlgorithm {
    public abstract byte[] encrypt(byte[] msg, byte[] key);
}
