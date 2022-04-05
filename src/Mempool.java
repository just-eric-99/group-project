import java.util.ArrayList;

public class Mempool {
    ArrayList<Transaction> mempool = new ArrayList<>();

    public void addToMempool(Transaction transaction){
        mempool.add(transaction);
    }
}
