package Block;

import Transaction.TxOut;
import util.HashUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Block {

    static ArrayList<Block> blockchain = new ArrayList<>();
    static int difficultyAdjustmentInterval = 10;
    static long blockGenerationInterval = 30000;
    public static HashMap<String, TxOut> UTXOs = new HashMap<String,TxOut>(); //list of all unspent transactions.

    String hash;
    String previousHash;
    String data;
    long timestamp;
    int index;
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
        Block latestBlock = blockchain.get(blockchain.size() - 1);
        if (latestBlock.index % difficultyAdjustmentInterval == 0 && latestBlock.index != 0) {
            return getAdjustedDifficulty(latestBlock);
        } else {
            return latestBlock.difficulty;
        }
    }

    static int getAdjustedDifficulty(Block latestBlock) {
        Block prevAdjustmentBlock = blockchain.get(blockchain.size() - difficultyAdjustmentInterval);
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

    public Block findBlock(int index, String previousHash, long timestamp, String data, int diff) {
        String prefix0 = HashUtils.getPrefix0(diff);
        int nonce = 0;

        String hash = calculateHash(index, previousHash, timestamp, data, nonce);
        while (true) {
            assert prefix0 != null;
            if (hash.startsWith(prefix0)) {
                return new Block(index, hash, previousHash, timestamp, data, diff, nonce);
            } else {
                nonce++;
                hash = calculateHash(index, previousHash, timestamp, data, nonce);
            }
        }
    }

    static boolean isValidBlock(Block newBlock) {
        Block previousBlock = blockchain.get(blockchain.size() - 1);
        if (previousBlock.index + 1 != newBlock.index) {
            System.out.println("index not match!");
            return false;
        }
        else if (!previousBlock.hash.equals(newBlock.previousHash)) {
            System.out.println("previous hash not match!");
            return false;
        }
        else if (!calculateHash(newBlock).equals(newBlock.hash)) {
            System.out.println("hash not match!");
            return false;
        }
        else return true;
    }

    public static void main(String[] args) {
        Block genesis = new Block(0, "", "0", new Date().getTime(), "This is genesis", 5, 0);

        blockchain.add(genesis);
        while (true) {
            long startTime = System.currentTimeMillis();
            Block lastBlock = blockchain.get(blockchain.size() - 1);


            int index = lastBlock.index + 1;
            String previousHash = lastBlock.hash;
            long timestamp = new Date().getTime();
            String data = "";
            int difficulty = getDifficulty();
            Block newBlock = lastBlock.findBlock(index, previousHash, timestamp, data, difficulty);
            blockchain.add(newBlock);

            long endTime = System.currentTimeMillis();
            long diffTime = endTime - startTime;
            System.out.println(diffTime/1000 + " seconds");
            System.out.println("current difficulty: " + newBlock.difficulty);
        }
    }
}
