package Wallet;

import util.ECDSAUtils;

import java.security.*;

public class Wallet {
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public Wallet(){
        generateKeyPair();
    }

    private void generateKeyPair(){
        try {
            KeyPair keypair = ECDSAUtils.getKeyPair();
            privateKey = keypair.getPrivate();
            publicKey = keypair.getPublic();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public PrivateKey getPrivateKey() { return this.privateKey; }
    public PublicKey getPublicKey() { return this.publicKey; }

}
