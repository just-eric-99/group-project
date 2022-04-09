import java.io.Serializable;

public class UTXO implements Serializable {
    String txOutId;
    long txOutIndex;
    String address;
    double amount;

    UTXO (String txOutId, long txOutIndex, String address, double amount){
        this.txOutId = txOutId;
        this.txOutIndex = txOutIndex;
        this.address = address;
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public long getTxOutIndex() {
        return txOutIndex;
    }

    public String getAddress() {
        return address;
    }

    public String getTxOutId() {
        return txOutId;
    }
}