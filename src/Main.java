import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.Timestamp;
import java.sql.SQLOutput;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.SerializationUtils;

public class Main {

    private DatagramSocket socket;
    private ServerSocket srvSocket;
    private
    int port;
    int portRange = 2000;
    public static boolean mining;

    public static ArrayList<Block> blockchain = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        new Main().runApp();
    }

    private void runApp() throws Exception {
        mining = false;
        initChain();
        mine();
    }

    private void mine() throws Exception {
        while (true) {
            while (!mining && blockchain.size() < 1) {
                continue;
            }
            Thread.sleep(1000);
            Block lastBlock = blockchain.get(blockchain.size() - 1);
            int index = lastBlock.index + 1;
            String previousHash = lastBlock.hash;
            long timestamp = new Date().getTime();
            //Add transactions later ...
            String data = "";
            int difficulty = Block.getDifficulty();

            Block newBlock = Block.findBlock(index, previousHash, timestamp, data, difficulty);
            //Add to blockchain
            if (newBlock != null) {
                blockchain.add(newBlock);
                System.out.println(blockchain);

                for (int i = 3000; i < 3000 + portRange; i++) {
                    if (i != port)
                        broadcast(SerializationUtils.serialize(blockchain), i);
                }
            }
        }
    }


    private void initChain() throws Exception {
        Random rand = new Random();
        port = rand.nextInt(portRange) + 3000;
        socket = new DatagramSocket(port);
        srvSocket = new ServerSocket(port);
        srvSocket.setSoTimeout(5000);

        System.out.println("My port: " + port);

        Thread udp = new Thread(() -> {
            Socket s = null;
            while (true) {
                try {
                    s = listen();
                    sendChain(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        udp.start();

        for (int i = 3000; i < 3000 + portRange; i++) {
            if (i != port)
                broadcast("Requesting chain".getBytes(), i);
        }

        Socket cSocket = null;
        try {
            cSocket = srvSocket.accept();
        } catch (Exception e) {
        }

        if (cSocket != null) {
            receiveChain(cSocket);
            System.out.println("Finished receiving");
        } else {
            Block genBlock = Block.generateGenesisBlock();
            blockchain.add(genBlock);
            System.out.println("Genesis block created");
            mining = true;
        }
    }

    //UDP
    private void broadcast(byte[] bytes, int port) throws Exception {
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, InetAddress.getLocalHost(), port);
        socket.send(packet);
    }

    private Socket listen() throws Exception {
        byte[] buffer = new byte[6400];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        byte[] data = packet.getData();
        Socket socket = new Socket(packet.getAddress(), packet.getPort());

        try {
            if (SerializationUtils.deserialize(data) instanceof ArrayList) {
                mining = false;
                ArrayList<Block> newChain = (ArrayList<Block>) SerializationUtils.deserialize(data);
                Block.replaceChain(newChain);
                mining = true;
            }
        } catch (Exception e) {
        }

        return socket;
    }

    //TCP
    private void sendChain(Socket soc) throws Exception {
        ObjectOutputStream out = new ObjectOutputStream(soc.getOutputStream());
        out.writeObject(blockchain);
    }

    private void receiveChain(Socket soc) throws Exception {
        mining = false;
        ObjectInputStream in = new ObjectInputStream(soc.getInputStream());
        Object inObject = in.readObject();
        blockchain = ((ArrayList<Block>) inObject);
        mining = true;
    }
}
