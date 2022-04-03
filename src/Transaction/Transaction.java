package Transaction;

import util.ECDSAUtils;
import util.HashUtils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;

public class Transaction {
    public String id;
    public PublicKey sender;
    public PublicKey receiver;
    public double value;
    public String signature;

    public ArrayList<TxIn> txIns = new ArrayList<>();
    public ArrayList<TxOut> txOuts = new ArrayList<>();

    public String getTransactionId(Transaction transaction){ //calculates txin txout content hash (used as id)
        String txInContent = transaction.txIns.stream()
                .map(txIn -> txIn.txOutID + txIn.txOutIndex).reduce("", (a,b) -> a+b); //.reduce("", String::concat);
        String txOutContent = transaction.txOuts.stream()
                .map(txOut -> txOut.address+txOut.amount).reduce("", String::concat);

        return HashUtils.getHashForStr(txInContent+txOutContent);
    }

    public void genSignature(PrivateKey privateKey){
        String data = ECDSAUtils.getStringFromKey(sender) + ECDSAUtils.getStringFromKey(receiver) + value;
        signature = ECDSAUtils.signECDSA(privateKey, data);
    }

    public boolean verifySignature() {
        String data = ECDSAUtils.getStringFromKey(sender) + ECDSAUtils.getStringFromKey(receiver) + value;
        return ECDSAUtils.verifyECDSA(sender, data, signature);
    }
}
