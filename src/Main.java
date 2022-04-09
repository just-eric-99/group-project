import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.SerializationUtils;
import util.HashUtils;


public class Main extends Application {

    private DatagramSocket socket;
    private ServerSocket srvSocket;
    private
    int port;
    int portRange = 2000;
    public Wallet minerWallet;

    public static final AtomicBoolean isMining = new AtomicBoolean(false);
    public static ArrayList<Block> blockchain = new ArrayList<>();
    Transaction coinbaseTransaction = new Transaction();
    public ArrayList<Transaction> mempool = new ArrayList<>();
    public static ArrayList<UTXO> utxos = new ArrayList<>();

    GUI gui;

    public static void main(String[] args) throws Exception {
        new Main().runApp();
    }

    @Override
    public synchronized void start(Stage primaryStage) throws Exception {
        Parent root;
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(GUI.class.getResource("GUI.fxml"));
        root = loader.load();
        gui = loader.getController();
        gui.initialize(minerWallet.getPublicKey(), minerWallet.getBalance(), this);
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.setTitle("Blockchain");
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
        primaryStage.show();
        this.notifyAll();
    }

    private synchronized void runApp() throws Exception {
        Random rand = new Random();
        port = rand.nextInt(portRange) + 3000;
        initWallet();

        Platform.runLater(() -> {
            try {
                start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        while (gui == null) {
            this.wait();
        }
        initChain();
    }

    private void initWallet() throws Exception {
        minerWallet = new Wallet(port);
    }

    public void mine() {
        Thread mine = new Thread(() -> {
            while (true) {
                if (!isMining.get()) {
                    continue;
                }
                Block lastBlock = blockchain.get(blockchain.size() - 1);
                int index = lastBlock.index + 1;
                String previousHash = lastBlock.hash;
                long timestamp = new Date().getTime();
                //Add transactions later ...
                String data = "";
                int difficulty = Block.getDifficulty();

                ArrayList<Transaction> currentTransactions = new ArrayList<>();
                currentTransactions.add(0, coinbaseTransaction.getCoinbaseTransaction(minerWallet.getPublicKey(), index));
                currentTransactions.addAll(mempool);

                data = new GsonBuilder().setPrettyPrinting().create().toJson(currentTransactions);

                Block newBlock = findBlock(index, previousHash, timestamp, data, difficulty);
                //Add to blockchain
                if (newBlock != null) {
                    blockchain.add(newBlock);
                    utxos = Transaction.updateUTXO(currentTransactions, utxos);
                    gui.updateBalanceInput(minerWallet.getBalance() + "");

                    String blockJson = "Block: " + new GsonBuilder().setPrettyPrinting().create().toJson(blockchain.get(blockchain.size() - 1)).replace("\\n", "\n").replace("\\", "");
                    gui.appendLog(blockJson);

                    mempool.removeAll(currentTransactions);

                    for (int i = 3000; i < 3000 + portRange; i++) {
                        if (i != port) {
                            Packet packet = new Packet(blockchain, mempool, utxos);
                            try {
                                broadcast(SerializationUtils.serialize(packet), i);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
        mine.start();
    }

    private void initChain() throws Exception {
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
            gui.appendLog("No existing node found");
        }

        if (cSocket != null) {
            receiveChain(cSocket);
            gui.appendLog("Chain received");
        } else {
            Block genBlock = Block.generateGenesisBlock();

            blockchain.add(genBlock);
            gui.appendLog("Genesis block created");
            gui.appendLog("Block: " + new GsonBuilder().setPrettyPrinting().create().toJson(genBlock).replace("\\n", "\n").replace("\\", ""));
            isMining.set(true);
        }
    }

    public void broadcast(byte[] bytes, int port) throws Exception {
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
            if (SerializationUtils.deserialize(data) instanceof Packet) {
                isMining.set(false);
                Packet cPacket = (Packet) SerializationUtils.deserialize(data);
                // replace mempool and blockchain at the same time
                replaceChain(cPacket);
                isMining.set(true);
            }

            if (SerializationUtils.deserialize(data) instanceof Transaction) {
                Transaction cTx = (Transaction) SerializationUtils.deserialize(data);
                mempool.add(cTx);
            }
        } catch (Exception e) {
            gui.appendLog("Peer connected");
        }
        return socket;
    }

    private void sendChain(Socket soc) throws Exception {
        ObjectOutputStream out = new ObjectOutputStream(soc.getOutputStream());
        Packet packet = new Packet(blockchain, mempool, utxos);
        out.writeObject(packet);
    }

    private void receiveChain(Socket soc) throws Exception {
        isMining.set(false);
        ObjectInputStream in = new ObjectInputStream(soc.getInputStream());
        Object inObject = in.readObject();
        Packet rPacket = (Packet) inObject;
        blockchain = rPacket.getBlockchain();
        mempool = rPacket.getMempool();
        utxos = rPacket.getUtxos();

        isMining.set(true);
    }

    public static Block findBlock(int index, String previousHash, long timestamp, String data, int diff) {
        String prefix0 = HashUtils.getPrefix0(diff);
        int nonce = 0;
        String hash = Block.calculateHash(index, previousHash, timestamp, data, nonce);
        while (Main.isMining.get()) {
            assert prefix0 != null;
            if (hash.startsWith(prefix0)) {
                return new Block(index, hash, previousHash, timestamp, data, diff, nonce);
            } else {
                nonce++;
                hash = Block.calculateHash(index, previousHash, timestamp, data, nonce);
            }
        }
        return null;
    }

    void replaceChain(Packet packet) {
        if (Block.isValidChain(packet.getBlockchain()) && packet.getBlockchain().size() > blockchain.size()) {
            gui.appendLog("Valid blockchain received. Replacing...");
            blockchain = packet.getBlockchain();
            mempool = packet.getMempool();
            utxos = packet.getUtxos();
            gui.updateBalanceInput(minerWallet.getBalance() + "");
        } else {
            gui.appendLog("Received blockchain invalid");
        }
    }
}
