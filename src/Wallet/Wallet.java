package Wallet;

import util.ECDSAUtils;

import java.security.*;

public class Wallet {
    public PrivateKey privateKey;
    public PublicKey publicKey;

    public Wallet(){
        generateKeyPair();
    }

    public void generateKeyPair(){
        try {
            KeyPair keypair = ECDSAUtils.getKeyPair();
            privateKey = keypair.getPrivate();
            publicKey = keypair.getPublic();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
