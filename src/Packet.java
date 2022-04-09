import java.io.Serializable;
import java.util.ArrayList;

public class Packet implements Serializable {
    private ArrayList<Block> blockchain;
    private ArrayList<Transaction> mempool;
    private ArrayList<UTXO> utxos;

    Packet(ArrayList<Block> blockchain, ArrayList<Transaction> mempool, ArrayList<UTXO> utxos) {
        this.blockchain = blockchain;
        this.mempool = mempool;
        this.utxos = utxos;
    }

    public ArrayList<Transaction> getMempool() {
        return mempool;
    }

    public ArrayList<Block> getBlockchain() {
        return blockchain;
    }

    public ArrayList<UTXO> getUtxos() {
        return utxos;
    }
}
