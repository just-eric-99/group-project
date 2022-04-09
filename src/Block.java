import com.google.gson.Gson;
import util.HashUtils;

import java.io.Serializable;
import java.util.*;

public class Block implements Serializable {

    static int difficultyAdjustmentInterval = 10;
    static long blockGenerationInterval = 30000;

    public static ArrayList<UTXO> utxos = new ArrayList<>();

    int index;
    String hash;
    String previousHash;
    String data;
    long timestamp;
    int difficulty;
    int nonce;

    public Block(int index, String hash, String previousHash, long timestamp, String data, int difficulty, int nonce) {
        this.index = index;
        this.hash = hash;
        this.previousHash = previousHash;
        this.timestamp = timestamp;
        this.data = data;
        this.difficulty = difficulty;
        this.nonce = nonce;
    }

    public static String calculateHash(int index, String previousHash, long timestamp, String data, int nonce) {
        return HashUtils.getHashForStr(index + previousHash + timestamp + data + nonce);
    }

    public static String calculateHash(Block block) {
        return HashUtils.getHashForStr(block.index + block.previousHash + block.timestamp + block.data + block.nonce);
    }

    public static int getDifficulty() {
        Block latestBlock = Main.blockchain.get(Main.blockchain.size() - 1);
        if (latestBlock.index % difficultyAdjustmentInterval == 0 && latestBlock.index != 0) {
            return getAdjustedDifficulty(latestBlock);
        } else {
            return latestBlock.difficulty;
        }
    }

    static int getAdjustedDifficulty(Block latestBlock) {
        Block prevAdjustmentBlock = Main.blockchain.get(Main.blockchain.size() - difficultyAdjustmentInterval);
        long timeExpected = blockGenerationInterval * difficultyAdjustmentInterval;
        long timeTaken = latestBlock.timestamp - prevAdjustmentBlock.timestamp;
        System.out.println("timeExpected: " + timeExpected);
        System.out.println("timeTaken: " + timeTaken);

        if (timeTaken < timeExpected / 2) {
            return prevAdjustmentBlock.difficulty + 1;
        } else if (timeTaken > timeExpected * 2) {
            return prevAdjustmentBlock.difficulty - 1;
        } else {
            return prevAdjustmentBlock.difficulty;
        }
    }

    public static Block findBlock(int index, String previousHash, long timestamp, String data, int diff) {
        String prefix0 = HashUtils.getPrefix0(diff);
        int nonce = 0;
        String hash = calculateHash(index, previousHash, timestamp, data, nonce);
        while (Main.isMining.get()) {
            assert prefix0 != null;
            if (hash.startsWith(prefix0)) {
                return new Block(index, hash, previousHash, timestamp, data, diff, nonce);
            } else {
                nonce++;
                hash = calculateHash(index, previousHash, timestamp, data, nonce);
            }
        } return null;
    }

    static Block generateGenesisBlock() {
        return new Block(0, "", "0", new Date().getTime(), "This is genesis block of hallelujah.", 6, 0);
    }

    static boolean isValidBlock(Block newBlock) {
        Block previousBlock = Main.blockchain.get(Main.blockchain.size() - 1);

        if (previousBlock.index + 1 != newBlock.index) {
            System.out.println("index not match!");
            return false;
        } else if (!previousBlock.hash.equals(newBlock.previousHash)) {
            System.out.println("previous hash not match!");
            return false;
        } else if (!calculateHash(newBlock).equals(newBlock.hash)) {
            System.out.println("hash not match!");
            return false;
        } else return true;
    }

    static boolean isValidChain(ArrayList<Block> chain) {
        if (!isValidGenesis(chain.get(0))) {
            return false;
        }
        return isValidBlock(chain.get(chain.size() - 1));
    }

    private static boolean isValidGenesis(Block block) {
        return Main.blockchain.get(0).toString().equals(block.toString());
    }

    static void replaceChain(Packet packet) {
        if (isValidChain(packet.getBlockchain()) && packet.getBlockchain().size() > Main.blockchain.size()) {
            System.out.println("Received blockchain is valid. Replacing current blockchain with received blockchain");
            Main.blockchain = packet.getBlockchain();
            Main.mempool = packet.getMempool();
        } else {
            System.out.println("Received blockchain invalid");
        }
    }

    @Override
    public String toString() {
        return "Block{" +
                "index=" + index +
                ", hash='" + hash + '\'' +
                ", previousHash='" + previousHash + '\'' +
                ", data='" + data + '\'' +
                ", timestamp=" + timestamp +
                ", difficulty=" + difficulty +
                ", nonce=" + nonce +
                '}';
    }
}
