package Transaction;

import util.ECDSAUtils;
import util.HashUtils;

import java.security.PublicKey;

public class TxOut {
    public String id;
    public String parentTransactionId; //the id of the transaction this output was created in
    public String address;  // String value of a public key of receiver
    public double amount;

    public TxOut(String add, double amt,String parentTransactionId){
        this.address = add;
        this.amount = amt;
        this.parentTransactionId = parentTransactionId;
        this.id = HashUtils.getHashForStr(address+amount+parentTransactionId);
    }

    public Boolean isMine(PublicKey publicKey){
        return address.equals(ECDSAUtils.getStringFromKey(publicKey));
    }

    public String toString(){ return id+parentTransactionId+address+amount; }
}
