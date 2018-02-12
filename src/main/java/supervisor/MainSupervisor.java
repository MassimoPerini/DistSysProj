package supervisor;

import operator.types.Sum;
import supervisor.communication.SocketListener;
import supervisor.communication.SocketManager;
import supervisor.communication.message.OperatorDeployment;
import utils.Debug;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by massimo on 21/12/17.
 */
public class MainSupervisor {
    public static void main(String[] args) {
        Debug.setLevel(Debug.LEVEL_VERBOSE);
        Debug.printVerbose("Main Supervisor Lanciato");

        SocketManager socketManager = new SocketManager();
        SocketListener socketListener = new SocketListener(socketManager);

        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(socketListener::run);

    }
}
