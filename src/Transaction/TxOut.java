package Transaction;

public class TxOut {
    public String address;
    public double amount;

    public TxOut(String add, double amt){
        this.address = add;
        this.amount = amt;
    }
}
