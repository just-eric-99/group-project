import util.HashUtils;

import java.math.BigInteger;
import java.util.ArrayList;

public class Block {
    //
    static ArrayList<Block> blockchain = new ArrayList<>();
    static int difficultyAdjustmentInterval = 10;
    static long blockGenerationInterval = 300000000;


    String hash;
    String previousHash;
    String data;
    long timestamp;
    static int nextIndex = 0;
    int index = 0;
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

        System.out.println(index);
    }

    public String calculateHash(int index, String previousHash, long timestamp, String data, int nonce) {
        return HashUtils.getHashForStr(index + previousHash + timestamp + data + nonce);
    }

    public static int getDifficulty() {
        Block latestBlock = blockchain.get(blockchain.size() - 1);
        if (latestBlock.index % difficultyAdjustmentInterval == 0 && latestBlock.index != 0) {
            System.out.println("hellofidhasfihpg");
            return getAdjustmentDifficulty(latestBlock);
        } else {
            return latestBlock.difficulty;
        }
    }

    static int getAdjustmentDifficulty(Block latestBlock) {
        Block prevAdjustmentBlock = blockchain.get(blockchain.size() - difficultyAdjustmentInterval);
        long timeExpected = blockGenerationInterval * difficultyAdjustmentInterval;
        long timeTaken = latestBlock.timestamp - prevAdjustmentBlock.timestamp;

        if (timeTaken < timeExpected / 2) {
            return prevAdjustmentBlock.difficulty + 1;
        } else if (timeTaken > timeExpected * 2) {
            return prevAdjustmentBlock.difficulty - 1;
        } else {
            return prevAdjustmentBlock.difficulty;
        }
    }

    public static boolean hashMatchesDifficulty(String hash, int difficulty) {
        String hashInBinary = new BigInteger(hash, 16).toString(2);
        hashInBinary = String.format("%256s", hashInBinary).replace(" ", "0");

        String requiredPrefix = "";
        for (int i = 0; i < difficulty; i++)
            requiredPrefix += "0";

        return hashInBinary.startsWith(requiredPrefix);
    }

    public Block findBlock(int index, String previousHash, long timestamp, String data, int diff) {
        int nonce = 0;

        while (true) {
            String hash = calculateHash(index, previousHash, timestamp, data, nonce);

            if (hashMatchesDifficulty(hash, diff)) {
                return new Block(index, hash, previousHash, timestamp, data, diff, nonce);
            } else {
                nonce++;
            }
        }

    }

    public static void main(String[] args) {
        Block genesis = new Block(0, "", "0", System.currentTimeMillis(), "This is genesis", 6, 0);

        blockchain.add(genesis);
        while (true) {
            Block lastBlock = blockchain.get(blockchain.size() - 1);

            int index = lastBlock.index + 1;
            String previousHash = lastBlock.hash;
            long timestamp = System.currentTimeMillis();
            String data = "";
            int difficulty = getDifficulty();

            blockchain.add(lastBlock.findBlock(index, previousHash, timestamp, data, difficulty));
        }
    }
}
