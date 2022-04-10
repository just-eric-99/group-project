import com.google.gson.Gson;
import util.ECDSAUtils;

import java.io.IOException;
import java.security.*;
import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;

import static util.ECDSAUtils.stringToKey;

public class Wallet {

    String keyPath;

    private PrivateKey privateKey = null;
    private PublicKey publicKey = null;

    public Wallet(int port) throws Exception{
        Gson gson = new Gson();
        keyPath = "key" + port + ".json";
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
        for (UTXO utxo : Main.utxos){
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

    public Transaction pay(String address, double amount) throws GeneralSecurityException, IOException {
        if (this.getBalance() < amount) {
            return null;
        }

        double total = 0;
        ArrayList<UTXO> myUTXOs = findMyUTXO();
        ArrayList<UTXO> refUTXOs = new ArrayList<>();
        ArrayList<TxIn> unsignedTxIns = new ArrayList<>();
        for (UTXO utxo : myUTXOs) {
            total += utxo.getAmount();
            TxIn unsignedTxIn = new TxIn(utxo.getTxOutId(), utxo.getTxOutIndex());

            unsignedTxIns.add(unsignedTxIn);
            refUTXOs.add(utxo);

            if (total > amount) break;
        }

        ArrayList<TxOut> txOuts = new ArrayList<>();
        TxOut toRecipient = new TxOut(address, amount);
        TxOut leftOver = new TxOut(ECDSAUtils.getStringFromKey(this.publicKey), total - amount);
        txOuts.add(toRecipient);
        txOuts.add(leftOver);

        Transaction transaction = new Transaction();
        transaction.txIns = unsignedTxIns;
        transaction.txOuts = txOuts;
        transaction.id = transaction.getTransactionId(transaction.txIns, transaction.txOuts);

        for(TxIn txin : transaction.txIns){
            txin.signature = Transaction.signTxIn(this, transaction, txin.txOutIndex, refUTXOs);
        }

        return transaction;
    }

    public String getPublicKey() {
        return ECDSAUtils.getStringFromKey(publicKey);
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }
}
