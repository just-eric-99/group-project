import java.net.InetAddress;

public class UDPPacket {
    private String data;
    private InetAddress address;
    private int port;

    public UDPPacket(String data, InetAddress address, int port){
        this.data = data;
        this.address = address;
        this.port = port;
    }

    public String getData() {
        return data;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
}
