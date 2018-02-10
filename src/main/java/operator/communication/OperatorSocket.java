package operator.communication;

import org.jetbrains.annotations.NotNull;
import supervisor.communication.SocketManager;

import java.net.Socket;

/**
 * Created by massimo on 10/02/18.
 */
public class OperatorSocket {

    private final Socket socket;

    public OperatorSocket(@NotNull Socket socket)
    {
        this.socket = socket;
    }

}
