package Transaction2;

import util.HashUtils;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

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
}

