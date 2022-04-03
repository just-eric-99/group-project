import util.HashUtils;

import java.util.Arrays;

public class Transaction {
    public String id;
    public TxIn[] txIns;
    public TxOut[] txOuts;

    public String getTransactionId(Transaction transaction){
        String txInContent = Arrays.stream(transaction.txIns)
                .map(txIn -> txIn.txOutID + txIn.txOutIndex).reduce("", (a,b) -> a+b); //.reduce("", String::concat);
        String txOutContent = Arrays.stream(transaction.txOuts)
                .map(txOut -> txOut.address+txOut.amount).reduce("", String::concat);

        return HashUtils.getHashForStr(txInContent+txOutContent);
    }
}
