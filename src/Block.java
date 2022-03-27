import util.HashUtils;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;

import com.google.gson.GsonBuilder;


public class Block {
    static ArrayList<Block> blockchain = new ArrayList<>();

    String hash;
    String previousHash;
    String data;
    long timestamp;
    static int nextIndex = 0;
    int index = 0;
    int difficulty = 6;
    int nonce;

    public Block(String data, String previousHash) {
        this.data = data;
        this.previousHash = previousHash;
        this.timestamp = System.currentTimeMillis()/1000;
        index = nextIndex++;
        this.hash = calculateHash();
        mineBlock();
    }

    public String calculateHash() {
        return HashUtils.getHashForStr(index + previousHash + timestamp + data);
    }

    void mineBlock() {
        powDemo(difficulty, this.hash);
    }

    public static String powDemo(int diff, String str){
        String prefix0 = HashUtils.getPrefix0(diff);
        System.out.println("prefix0: " + prefix0);
        int nonce = 0;

        String hash = HashUtils.getHashForStr(str);
        while(true){
            assert prefix0 != null;
            if(hash.startsWith(prefix0)){
                System.out.println("Find target!");
                System.out.println("hash: " + hash);
                System.out.println("nonce: " + nonce);
                return hash;
            }else {
                nonce++;
                hash = HashUtils.getHashForStr(str + nonce);
            }
        }
    }

    public static void main(String[] args) {
        Block genesis = new Block("First block", "0");
//        System.out.println("Genesis previous hash: " + genesis.previousHash);
//        System.out.println("Genesis hash: " + genesis.hash);
//        System.out.println("Genesis index: " + genesis.index);
//        System.out.println();

        Block secondBlock = new Block("Second block", genesis.hash);
//        System.out.println("Second block previous hash: " + secondBlock.previousHash);
//        System.out.println("Second block hash: " + secondBlock.hash);
//        System.out.println("Second block index: " + secondBlock.index);
//        System.out.println();

        Block thirdBlock = new Block("Third block", secondBlock.hash);
//        System.out.println("Third block previous hash: " + thirdBlock.previousHash);
//        System.out.println("Third block hash: " + thirdBlock.hash);
//        System.out.println("Third block index: " + thirdBlock.index);

        blockchain.add(genesis);
        blockchain.add(secondBlock);
        blockchain.add(thirdBlock);


        String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
        System.out.println(blockchainJson);



    }
}
