package Transaction;

public class TxIn {
    public String txOutID;
//    public double txOutIndex; //is this suppposed to be double, int, or long?
//    public String signature;
    public TxOut UTXO;

    public TxIn(String transactionOutputId) {
        this.txOutID = transactionOutputId;
    }
}
