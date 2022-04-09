import util.HashUtils;

import java.io.Serializable;
import java.util.*;

public class Block implements Serializable {

    static int difficultyAdjustmentInterval = 10;
    static long blockGenerationInterval = 30000;
    static double coinbaseAmount = 50;

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
        while (Main.mining) {
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
        return new Block(0, "", "0", new Date().getTime(), "This is genesis", 6, 0);
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

    static void replaceChain(ArrayList<Block> newChain) {
        if (isValidChain(newChain) && newChain.size() > Main.blockchain.size()) {
            System.out.println("Received blockchain is valid. Replacing current blockchain with received blockchain");
            Main.blockchain = newChain;
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

//        public static void main(String[] args) {
//        Block genesis = new Block(0, "", "0", new Date().getTime(), "This is genesis", 5, 0);
//
//        Wallet miner = new Wallet();
//        blockchain.add(genesis);
//        while (true) {
//            long startTime = System.currentTimeMillis();
//            Block lastBlock = blockchain.get(blockchain.size() - 1);
//
//            int index = lastBlock.index + 1;
//            String previousHash = lastBlock.hash;
//            long timestamp = new Date().getTime();
//
//            ArrayList<TxIn> coinBaseTxIns = new ArrayList<>();
//            coinBaseTxIns.add(new TxIn(String.valueOf(index), 0,""));
//            ArrayList<TxOut> coinBaseTxOuts = new ArrayList<>();
//            //add transaction fee as well
//            coinBaseTxOuts.add(new TxOut(ECDSAUtils.getStringFromKey(miner.getPublicKey()), coinbaseAmount));
//            Transaction coinbaseTransaction = new Transaction(coinBaseTxIns,coinBaseTxOuts);
//
//            ArrayList<Transaction> temp = new ArrayList<>();
//            temp.add(coinbaseTransaction);
//            temp.addAll(mempool);
//            String data = new Gson().toJson(temp);
//
//            int difficulty = getDifficulty();
//            Block newBlock = lastBlock.findBlock(index, previousHash, timestamp, data, difficulty);
//            blockchain.add(newBlock);
//
//            long endTime = System.currentTimeMillis();
//            long diffTime = endTime - startTime;
//            System.out.println(diffTime/1000 + " seconds");
//            System.out.println("current difficulty: " + newBlock.difficulty);
//        }
//    }
}
