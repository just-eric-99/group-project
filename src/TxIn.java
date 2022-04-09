import java.io.Serializable;

public class TxIn implements Serializable {
    public String txOutId;
    public long txOutIndex;
    public String signature;

    public TxIn(String txOutId, long txOutIndex, String signature) {
        this.txOutId = txOutId;
        this.txOutIndex = txOutIndex;
        this.signature = signature;
    }

    public TxIn(String txOutId, long txOutIndex) {
        this.txOutId = txOutId;
        this.txOutIndex = txOutIndex;
    }
}
