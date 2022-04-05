import com.google.gson.Gson;
import util.ECDSAUtils;

import java.security.*;
import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;

import static util.ECDSAUtils.stringToKey;

public class Wallet {

    String keyPath = "key.json";

    private PrivateKey privateKey = null;
    private PublicKey publicKey = null;

    public Wallet() throws Exception{
        Gson gson = new Gson();
        File f = new File(keyPath);
        if(f.isFile()) {
            Map<?, ?> map = gson.fromJson(new FileReader(keyPath), Map.class);
            privateKey = (PrivateKey) stringToKey(map.get("privateKey").toString(), true);
            publicKey = (PublicKey) stringToKey(map.get("publicKey").toString(), false);
            return;
        }
        Map<String, String> keyPair = generateKeyPair();
        FileWriter fileWriter = new FileWriter(keyPath);
        gson.toJson(keyPair, fileWriter);
        fileWriter.close();
    }

    private Map<String, String> generateKeyPair() throws Exception{
        KeyPair keypair = ECDSAUtils.getKeyPair();
        privateKey = keypair.getPrivate();
        publicKey = keypair.getPublic();
        Map<String, String> keyMap = new HashMap<>();
        keyMap.put("privateKey", ECDSAUtils.getStringFromKey(privateKey));
        keyMap.put("publicKey", ECDSAUtils.getStringFromKey(publicKey));
        return keyMap;
    }

    public double getBalance() {
        ArrayList<UTXO> myUTXOs = findMyUTXO();
        double balance = 0;
        for (UTXO utxo : myUTXOs) {
            balance += utxo.getAmount();
        }
        return balance;
    }

    public ArrayList<UTXO> findMyUTXO() {
        ArrayList<UTXO> myUTXOs = new ArrayList<>();
        String myAddress = ECDSAUtils.getStringFromKey(publicKey);
        for (UTXO utxo : Block.utxos){
            if (utxo.getAddress().equals(myAddress)){
                myUTXOs.add(utxo);
            }
        }
        return myUTXOs;
    }

    boolean deleteWallet() {
        publicKey = null;
        privateKey= null;
        File keyFile = new File(keyPath);
        return keyFile.delete();
    }

    public Transaction pay(String address, double amount) {
        // get balance();
        if (getBalance() < amount) {
            System.out.println("Insufficient fund to send transaction");
            return null;
        }

        double total = 0;
        ArrayList<UTXO> myUTXOs = findMyUTXO();
        ArrayList<UTXO> refUTXOs = new ArrayList<>();
        ArrayList<TxIn> unsignedTxIns = new ArrayList<>();
        for (UTXO utxo : myUTXOs) {
            total += utxo.getAmount();
            // fixme signature to be verify
            TxIn unsignedTxIn = new TxIn(utxo.getTxOutId(), utxo.getTxOutIndex());

            unsignedTxIns.add(unsignedTxIn);
            refUTXOs.add(utxo);

            if (total > amount) break;
        }

        // send amount to address
        ArrayList<TxOut> txOuts = new ArrayList<>();
        TxOut toRecipient = new TxOut(address, amount);
        TxOut leftOver = new TxOut(ECDSAUtils.getStringFromKey(this.publicKey), total - amount);
        txOuts.add(toRecipient);
        txOuts.add(leftOver);

        // fixme transaction id has to be sign
        Transaction transaction = new Transaction();
        transaction.txIns = unsignedTxIns;
        transaction.txOuts = txOuts;
        transaction.id = transaction.getTransactionId();

        for(TxIn txin : transaction.txIns){
            txin.signature = Transaction.signTxIn(transaction, txin.txOutIndex, privateKey, refUTXOs);
        }

        return transaction;
    }
}
