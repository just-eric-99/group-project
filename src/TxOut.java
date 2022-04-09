import java.io.Serializable;

public class TxOut implements Serializable {
    public String address;
    public double amount;

    public TxOut(String address, double amount) {
        this.address = address;
        this.amount = amount;
    }
}