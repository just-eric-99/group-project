package Wallet;

import util.ECDSAUtils;

import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Wallet {
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public HashMap<String, Long> utxos = new HashMap<>();

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

    public double getBalance () {
        double total = 0;

        for(Map.Entry<String, Long> utxo: Block.utxos.entrySet()) {
            System.out.println("utxo.getValue().address: " + utxo.getValue().address);
            System.out.println("publicKey.toString(): " + ECDSAUtils.getStringFromKey(publicKey));

            if(utxo.getk().address.equals(ECDSAUtils.getStringFromKey(publicKey))) {
                utxos.put(utxo.getKey(), utxo.getValue());
                total += utxo.getValue().amount;
            }
        }

        return total;
    }

    public Transaction pay(PublicKey pk, double amount) {
        //if(getBalance < amount)

        ArrayList<TxIn> txIns = new ArrayList<>();
        double total = 0;
        for (Map.Entry<String, TxOut> utxo: utxos.entrySet()) {
            total += utxo.getValue().amount;


            txIns.add(new TxIn(utxo.getKey(), utxo.getValue(), ECDSAUtils.signECDSA(privateKey, "")));

            if (total > amount) break;
        }

        return
    }

    public PrivateKey getPrivateKey() { return this.privateKey; }
    public PublicKey getPublicKey() { return this.publicKey; }

}
