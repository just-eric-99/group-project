import java.io.Serializable;
import java.util.ArrayList;

public class Packet implements Serializable {
    private ArrayList<Block> blockchain;
    private ArrayList<Transaction> mempool;

    Packet(ArrayList<Block> blockchain, ArrayList<Transaction> mempool) {
        this.blockchain = blockchain;
        this.mempool = mempool;
    }

    public ArrayList<Transaction> getMempool() {
        return mempool;
    }

    public ArrayList<Block> getBlockchain() {
        return blockchain;
    }
}
