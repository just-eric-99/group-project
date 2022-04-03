package Transaction;

import util.HashUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class Transaction {
    public String id;
    public ArrayList<TxIn> txIns = new ArrayList<>();
    public ArrayList<TxOut> txOuts = new ArrayList<>();

    public String getTransactionId(Transaction transaction){ //refers to id of next transaction block?
        String txInContent = transaction.txIns.stream()
                .map(txIn -> txIn.txOutID + txIn.txOutIndex).reduce("", (a,b) -> a+b); //.reduce("", String::concat);
        String txOutContent = transaction.txOuts.stream()
                .map(txOut -> txOut.address+txOut.amount).reduce("", String::concat);

        return HashUtils.getHashForStr(txInContent+txOutContent);
    }
}
