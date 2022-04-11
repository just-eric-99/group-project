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
import util.HashUtils;


public class Main extends Application implements Serializable {

    private DatagramSocket socket;
    private ServerSocket srvSocket;
    int port;
    int portRange = 2000;
    public Wallet minerWallet;

    public final AtomicBoolean isMining = new AtomicBoolean(false);
    public static ArrayList<Block> blockchain = new ArrayList<>();
    Transaction coinbaseTransaction = new Transaction();
    public ArrayList<Transaction> mempool = new ArrayList<>();
    public static ArrayList<UTXO> utxos = new ArrayList<>();

    public final ArrayList<Socket> socketList = new ArrayList<>();

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
        primaryStage.setResizable(false);
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
                if(!isMining.get()){
                    continue;
                }
                Block lastBlock = blockchain.get(blockchain.size() - 1);
                int index = lastBlock.index + 1;
                String previousHash = lastBlock.hash;
                long timestamp = System.currentTimeMillis() / 1000L;
                int difficulty = Block.getDifficulty();

                ArrayList<Transaction> currentTransactions = new ArrayList<>();
                currentTransactions.add(0, coinbaseTransaction.getCoinbaseTransaction(minerWallet.getPublicKey(), index));
                currentTransactions.addAll(mempool);

                String data = new GsonBuilder().setPrettyPrinting().create().toJson(currentTransactions);

                Block newBlock = findBlock(index, previousHash, timestamp, data, difficulty);

                if (newBlock != null) {
                    blockchain.add(newBlock);
                    utxos = Transaction.updateUTXO(currentTransactions, utxos);
                    gui.updateBalanceInput(minerWallet.getBalance() + "");

                    String blockJson = "Block: " + new GsonBuilder().setPrettyPrinting().create().toJson(blockchain.get(blockchain.size() - 1)).replace("\\n", "\n").replace("\\", "");
                    gui.appendHistory(blockJson);
                    gui.appendLog("New block found");

                    mempool.removeAll(currentTransactions);

                    synchronized (socketList) {
                        socketList.parallelStream().forEach(cSocket -> {
                            try {
                                sendChain(cSocket);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            }
        });
        mine.start();
    }

    private void initChain() throws Exception {
        socket = new DatagramSocket(port);
        srvSocket = new ServerSocket(port);

        for (int i = 3000; i < 3000 + portRange; i++) {
            send("Requesting chain".getBytes(), InetAddress.getLocalHost(), i);
        }

        socket.setSoTimeout(1000);
        while (true) {
            try {
                UDPPacket udpPacket = listen();
                if (udpPacket.getData().equals("Response")) {
                    socketList.add(new Socket(udpPacket.getAddress(), udpPacket.getPort()));
                }
            } catch (Exception e) {
                break;
            }
        }

        socket.setSoTimeout(0);
        Thread udp = new Thread(() -> {
            while (true) {
                try {
                    UDPPacket udpPacket = listen();
                    if (udpPacket.getData().equals("Requesting chain")) {
                        send("Response".getBytes(), udpPacket.getAddress(), udpPacket.getPort());
                    }
                } catch (Exception e) {

                }
            }
        });
        udp.start();

        for (Socket s : socketList){
            new Thread(() -> {
                while (true) {
                    try {
                        receive(s);
                    } catch (Exception e) {

                    }
                }
            }).start();
        }

        Thread server = new Thread(() -> {
            while (true) {
                try {
                    Socket cSocket = srvSocket.accept();
                    synchronized (socketList) {
                        socketList.add(cSocket);
                    }
                    sendChain(cSocket);
                    Thread t  = new Thread(() -> {
                        while (true) {
                            try {
                                receive(cSocket);
                            } catch (Exception e){

                            }
                        }
                    });
                    t.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        server.start();


        if(socketList.size() == 0) {
            gui.appendLog("No existing node found");
            Block genBlock = Block.generateGenesisBlock();
            blockchain.add(genBlock);
            gui.appendLog("Genesis block created");
            gui.appendHistory("Block: " + new GsonBuilder().setPrettyPrinting().create().toJson(genBlock).replace("\\n", "\n").replace("\\", ""));
            isMining.set(true);
        }
    }

    public void send(byte[] bytes, InetAddress address, int port) throws Exception {
        if (port != this.port) {
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);
            socket.send(packet);
        }
    }

    private UDPPacket listen() throws Exception {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        String msg = new String(packet.getData(), 0, packet.getLength());
        return new UDPPacket(msg, packet.getAddress(), packet.getPort());
    }

    private void sendChain(Socket soc) throws Exception {
        ObjectOutputStream out = new ObjectOutputStream(soc.getOutputStream());
        Packet packet = new Packet(blockchain, mempool, utxos);
        out.writeObject(packet);
    }

    protected void sendTransaction(Transaction tx, Socket soc) throws Exception {
        ObjectOutputStream out = new ObjectOutputStream(soc.getOutputStream());
        out.writeObject(tx);
    }

    private void receive(Socket soc) throws Exception {
        ObjectInputStream in = new ObjectInputStream(soc.getInputStream());
        Object inObject = in.readObject();
        if(inObject instanceof Packet) {
            isMining.set(false);
            Packet rPacket = (Packet) inObject;
            replaceChain(rPacket);
            isMining.set(true);
        }
        if(inObject instanceof Transaction) {
            Transaction cTx = (Transaction) inObject;
            mempool.add(cTx);
        }
    }

    public Block findBlock(int index, String previousHash, long timestamp, String data, int diff) {
        String prefix0 = HashUtils.getPrefix0(diff);
        int nonce = 0;
        String hash = Block.calculateHash(index, previousHash, timestamp, data, nonce);
        gui.appendLog("Mining new block...");
        while (isMining.get()) {
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

    synchronized void replaceChain(Packet packet) {
        if (blockchain.size() == 0) {
            blockchain = packet.getBlockchain();
            mempool = packet.getMempool();
            utxos = packet.getUtxos();
            gui.appendLog("Chain received");
        } else if (Block.isValidChain(packet.getBlockchain()) && packet.getBlockchain().size() > blockchain.size()) {
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
