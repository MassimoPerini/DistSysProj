package supervisor;

import supervisor.communication.SocketListener;
import supervisor.communication.SocketManager;
import supervisor.graph_representation.Graph;
import utils.Debug;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import operator.types.OperatorType;

/**
 * Created by massimo on 21/12/17.
 */
public class MainSupervisor {
    public static void main(String[] args) {
        Debug.setLevel(Debug.LEVEL_VERBOSE);
        Debug.printVerbose("Main Supervisor Lanciato");


        SocketManager socketManager = new SocketManager();
        SocketListener socketListener = new SocketListener(socketManager);
        InputReceiver receiver= new InputReceiver(socketListener);
        
        
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(socketListener::run);
        executorService.submit(receiver::run);
    }
}
