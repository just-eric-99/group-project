import util.ECDSAUtils;
import util.HashUtils;

import java.io.IOException;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;

import static util.ECDSAUtils.stringToKey;

public class Transaction {

    public String id;
    public ArrayList<TxIn> txIns = new ArrayList<>();
    public ArrayList<TxOut> txOuts = new ArrayList<>();

    int coinbaseAmount = 50;

//    public Transaction(ArrayList<TxIn> txIns, ArrayList<TxOut> txOuts){
//        this.txIns = txIns;
//        this.txOuts = txOuts;
//    }

    public String getTransactionId() {
        String txInContent = txIns.stream().map(txIn -> txIn.txOutId + txIn.txOutIndex).reduce("", (a, b) -> a+b);
        String txOutContent = txOuts.stream().map(txOut -> txOut.address + txOut.amount).reduce("", (a, b) -> a+b);

        return HashUtils.getHashForStr(txInContent + txOutContent);
    }
    
    public static String signTxIn(Transaction transaction, long txInIndex, PrivateKey privateKey, ArrayList<UTXO> UTXOList){
        TxIn txIn = transaction.txIns.get((int)txInIndex);
        String dataToSign = transaction.id;
        UTXO referencedUTXO = findUTXO(txIn.txOutId, txIn.txOutIndex, UTXOList);
        if(referencedUTXO == null){
            System.out.println("Could not find referenced txOut");
            throw new Error();
        }

        String referencedAddress =referencedUTXO.address;

        if(!ECDSAUtils.getStringFromKey(privateKey).equals(referencedAddress)){
            System.out.println("Trying to sign an input with private key that does not match the address that is referenced in txIn");
            throw new Error();
        }

        String signature = ECDSAUtils.signECDSA(privateKey, dataToSign);

        return HashUtils.byteToHex(signature.getBytes());
    }

    public ArrayList<UTXO> updateUTXO(ArrayList<Transaction> txs, ArrayList<UTXO> currentUTXOs) {
        ArrayList<UTXO> newUTXOs = new ArrayList<>();
        ArrayList<UTXO> usedTxOuts = new ArrayList<>();
        ArrayList<UTXO> resultingUTXOs = new ArrayList<>();

        for(Transaction tx : txs) {
            final int[] i = {0};
            // add to newUTXOs
            tx.txOuts.forEach(txOut -> {
                newUTXOs.add(new UTXO(tx.id, i[0], txOut.address, txOut.amount));
                i[0] = i[0] + 1;
            });
            // add to usedUTXOS
            tx.txIns.forEach(txIn -> usedTxOuts.add(new UTXO(txIn.txOutId, txIn.txOutIndex, "", 0)));
        }

        currentUTXOs.stream().filter(x -> findUTXO(x.txOutId, x.txOutIndex, usedTxOuts) == null).forEach(resultingUTXOs::add);
        // ==  null or != null ???
        //from currentUTXOs, filter out those that are in UsedTxOuts, then add to resultingUTXOs.
        resultingUTXOs.addAll(newUTXOs);

        return resultingUTXOs;
    }

    static UTXO findUTXO(String transactionId, long index, ArrayList<UTXO> UTXOList){
        for(UTXO utxo : UTXOList){
            if(utxo.txOutId.equals(transactionId) && utxo.txOutIndex == index){
                return utxo;
            }
        }
        return null;
    }

    Transaction getCoinbaseTransaction (String address, int blockIndex) {
        Transaction temp = new Transaction();
        temp.txIns.add(new TxIn("", blockIndex,""));
        temp.txOuts.add(new TxOut(address, coinbaseAmount));
        temp.id = getTransactionId();

        return temp;
    }

    // fixme
    boolean validateTransaction (Transaction transaction, ArrayList<UTXO> UTXOList){




        return true;
    }

    boolean validateTxIn(TxIn txIn, Transaction transaction, ArrayList<UTXO> UTXOList) throws GeneralSecurityException, IOException{
        UTXO referencedTxOut = null;

        for(UTXO utxo : UTXOList){
            if(utxo.txOutId.equals(txIn.txOutId) && utxo.txOutIndex == txIn.txOutIndex){
                referencedTxOut=utxo;
            }
        }

        if(referencedTxOut==null){
            return false;
        }

        String address = referencedTxOut.address;

        if(!ECDSAUtils.verifyECDSA((PublicKey) stringToKey(address, false), txIn.signature, "")){
            System.out.println("Invalid txIn signature");
            return false;
        }

        return true;

    }
}
