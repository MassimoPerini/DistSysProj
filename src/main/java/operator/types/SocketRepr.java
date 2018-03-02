package operator.types;

import java.net.InetAddress;

/**
 * Created by massimo on 02/03/18.
 */
public class SocketRepr {

    private InetAddress address;
    private int port;

    public SocketRepr(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
}
