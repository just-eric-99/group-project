package Transaction;

import util.HashUtils;

import java.util.ArrayList;

public class Transaction {

    public String id;
    public ArrayList<TxIn> txIns = new ArrayList<>();
    public ArrayList<TxOut> txOuts = new ArrayList<>();

    public Transaction(ArrayList<TxIn> txIns, ArrayList<TxOut> txOuts){
        this.txIns = txIns;
        this.txOuts = txOuts;
    }

    String getTransactionId (Transaction transaction) {
        String txInContent = txIns.stream().map(txIn -> txIn.txOutId + txIn.txOutIndex).reduce("", (a, b) -> a+b);
        String txOutContent = txOuts.stream().map(txOut -> txOut.address + txOut.amount).reduce("", (a, b) -> a+b);

        return HashUtils.getHashForStr(txInContent + txOutContent);
    }
    
    String signTxIn(Transaction transaction, long txInIndex, String privateKey, ArrayList<UTXO> UTXOList){
        TxIn txIn = transaction.txIns.get((int)txInIndex);
        String dataToSign = transaction.id;
        //fixme
        //add findUXTO function
        UTXO referencedUTXO = findUTXO(txIn.txOutId, txIn.txOutIndex, UTXOList);
        String sign = HashUtils.getHashForStr()
    }

}

class TxIn {
    public String txOutId;
    public long txOutIndex;
    public String signature;

    public TxIn(String txOutId, long txOutIndex, String signature) {
        this.txOutId = txOutId;
        this.txOutIndex = txOutIndex;
        this.signature = signature;
    }
}

class TxOut {
    public String address;
    public double amount;

    public TxOut(String address, double amount) {
        this.address = address;
        this.amount = amount;
    }
}

class UTXO {
    private String txOutId;
    private long txOutIndex;
    private String address;
    private double amount;

    UTXO (String txOutId, long txOutIndex, String address, double amount){
        this.txOutId = txOutId;
        this.txOutIndex = txOutIndex;
        this.address = address;
        this.amount = amount;
    }


}

