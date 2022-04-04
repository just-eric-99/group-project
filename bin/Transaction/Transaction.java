package Transaction;

import util.ECDSAUtils;
import util.HashUtils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import Block.Block;
import java.util.Arrays;

public class Transaction {
    public String id;
    public PublicKey sender;
    public PublicKey receiver;
    public double value;
    public String signature;

    public ArrayList<TxIn> txIns = new ArrayList<>();
    public ArrayList<TxOut> txOuts = new ArrayList<>();

    public Transaction(PublicKey from, PublicKey to, float value,  ArrayList<TxIn> inputs) {
        this.sender = from;
        this.receiver = to;
        this.value = value;
        this.txIns = inputs;
    }

    public String getTransactionId(){ //calculates txin txout content hash (used as id)
        String txInContent = txIns.stream()
                .map(txIn -> txIn.txOutID + txIn.UTXO.toString()).reduce("", (a,b) -> a+b); //.reduce("", String::concat);
        //txIn -> txIn.txOutID + txIn.txOutIndex
        //there is no txOutContent because txOut is only generated after transactionId is found.
//        String txOutContent = txOuts.stream()
//                .map(txOut -> txOut.address+txOut.amount).reduce("", String::concat);

        return HashUtils.getHashForStr(txInContent);
    }

    public void genSignature(PrivateKey privateKey){
        String data = ECDSAUtils.getStringFromKey(sender) + ECDSAUtils.getStringFromKey(receiver) + value;
        signature = ECDSAUtils.signECDSA(privateKey, data);
    }

    public boolean verifySignature() {
        String data = ECDSAUtils.getStringFromKey(sender) + ECDSAUtils.getStringFromKey(receiver) + value;
        return ECDSAUtils.verifyECDSA(sender, data, signature);
    }

    public boolean processTransaction() {

        if(verifySignature() == false) {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }

        //gather transaction inputs (Make sure they are unspent):
        for(TxIn i : txIns) {
            i.UTXO = Block.UTXOs.get(i.txOutID);
        }

        //check if transaction is valid:
//        if(getInputsValue() < Block.minimumTransaction) {
//            System.out.println("#Transaction Inputs to small: " + getInputsValue());
//            return false;
//        }

        //generate transaction outputs:
        double leftOver = getInputsValue() - value; //get value of inputs then the left over change:
        id = getTransactionId();
        txOuts.add(new TxOut( ECDSAUtils.getStringFromKey(this.receiver), value,id)); //send value to recipient
        txOuts.add(new TxOut( ECDSAUtils.getStringFromKey(this.sender), leftOver,id)); //send the left over 'change' back to sender

        //add outputs to Unspent list
        for(TxOut o : txOuts) {
            Block.UTXOs.put(o.id , o);
        }

        //remove transaction inputs from UTXO lists as spent:
        for(TxIn i : txIns) {
            if(i.UTXO == null) continue; //if Transaction can't be found skip it
            Block.UTXOs.remove(i.UTXO.id);
        }

        return true;
    }

    //returns sum of inputs(UTXOs) values
    public float getInputsValue() {
        float total = 0;
        for(TxIn i : txIns) {
            if(i.UTXO == null) continue; //if Transaction can't be found skip it
            total += i.UTXO.amount;
        }
        return total;
    }

    //returns sum of outputs:
    public float getOutputsValue() {
        float total = 0;
        for(TxOut o : txOuts) {
            total += o.amount;
        }
        return total;
    }
}
