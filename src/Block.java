import util.HashUtils;

import java.io.Serializable;
import java.util.*;

public class Block implements Serializable {

    static int difficultyAdjustmentInterval = 10;
    static long blockGenerationInterval = 30;

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

    static Block generateGenesisBlock() {
        return new Block(0, "", "", System.currentTimeMillis() / 1000L, "This is COMP4137 genesis block", 6, 0);
    }

    static boolean isValidBlock(Block newBlock) {
        Block previousBlock = Main.blockchain.get(Main.blockchain.size() - 1);

        if (previousBlock.index + 1 != newBlock.index) {
            System.out.println("Index not match");
            return false;
        } else if (!previousBlock.hash.equals(newBlock.previousHash)) {
            System.out.println("Previous hash not match");
            return false;
        } else if (!calculateHash(newBlock).equals(newBlock.hash)) {
            System.out.println("Hash not match");
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
