package operator.communication.message;

import java.io.Serializable;

/**
 * Created by massimo on 02/03/18.
 */
@Deprecated
public class MessageData implements Serializable {
    //todo: add key
    private String ipAddr;
    private String port;
    private int counter;
    private final double value;


    @Override
    public String toString() {
        return "MessageData{" +
                "ipAddr='" + ipAddr + '\'' +
                ", port='" + port + '\'' +
                ", counter=" + counter +
                ", value=" + value +
                '}';
    }

    public MessageData (String ipAddr, String port, int counter, double d)
    {
        this.ipAddr = ipAddr;
        this.port = port;
        this.counter = counter;
        this.value = d;
    }

    public MessageData(double value) {
        this.value = value;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public String getPort() {
        return port;
    }

    public int getCounter() {
        return counter;
    }

    public double getValue() {
        return value;
    }
}
