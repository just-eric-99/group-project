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


public class Main extends Application {

    private DatagramSocket socket;
    private ServerSocket srvSocket;
    private
    int port;
    int portRange = 2000;
    Wallet minerWallet;

    public static final AtomicBoolean isMining = new AtomicBoolean(false);
    public static ArrayList<Block> blockchain = new ArrayList<>();
    Transaction coinbaseTransaction = new Transaction();
    public static ArrayList<Transaction> mempool = new ArrayList<>();

    private GUI gui;

    public static void main(String[] args) throws Exception {
        new Main().runApp();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
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
    }

    private void runApp() throws Exception {
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

        initChain();
        //mine();

        //minerWallet.pay("MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCBklO9a2Cra5bwKlatwjGja+HoohB6ZpjQsoQbmvYGT0Q\u003d\u003d", 1);
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
                currentTransactions.add(0, coinbaseTransaction.getCoinbaseTransaction(String.valueOf(minerWallet.getPublicKey())));
                data = new GsonBuilder().setPrettyPrinting().create().toJson(currentTransactions);

                Block newBlock = Block.findBlock(index, previousHash, timestamp, data, difficulty);
                //Add to blockchain
                if (newBlock != null) {
                    blockchain.add(newBlock);
                    String blockJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain.get(blockchain.size() - 1)).replace("\n       ", "");
                    System.out.println(blockJson);
                    gui.appendLog(blockJson);

                    for (int i = 3000; i < 3000 + portRange; i++) {
                        if (i != port) {
                            Packet packet = new Packet(blockchain, mempool);
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
            System.out.println("No existing node found");
        }

        if (cSocket != null) {
            receiveChain(cSocket);
            System.out.println("Finished receiving");
        } else {
            Block genBlock = Block.generateGenesisBlock();

            blockchain.add(genBlock);
            System.out.println("Genesis block created");
            System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(genBlock).replace("\n       ", ""));
            isMining.set(true);
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
            if (SerializationUtils.deserialize(data) instanceof Packet) {
                isMining.set(false);
                Packet cPacket = (Packet) SerializationUtils.deserialize(data);
                // replace mempool and blockchain at the same time
                Block.replaceChain(cPacket);

                isMining.set(true);
            }
        } catch (Exception e) {
            System.out.println("Someone connected");
        }
        return socket;
    }

    //TCP
    private void sendChain(Socket soc) throws Exception {
        ObjectOutputStream out = new ObjectOutputStream(soc.getOutputStream());
        Packet packet = new Packet(blockchain, mempool);
        out.writeObject(packet);
    }

    private void receiveChain(Socket soc) throws Exception {
        isMining.set(false);
        ObjectInputStream in = new ObjectInputStream(soc.getInputStream());
        Object inObject = in.readObject();
        Packet packet = (Packet) inObject;
        blockchain = packet.getBlockchain();
        mempool = packet.getMempool();
        isMining.set(true);
    }
}
