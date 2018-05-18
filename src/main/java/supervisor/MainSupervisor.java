package supervisor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import supervisor.communication.SocketListener;
import supervisor.communication.SocketManager;
import utils.Debug;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by massimo on 21/12/17.
 */
public class MainSupervisor {
    public static void main(String[] args) {
        Debug.setLevel(Debug.LEVEL_VERBOSE);

        Logger logger = LogManager.getLogger();
        ThreadContext.put("logFileName", "supervisor");

        logger.info("Main Supervisor Lanciato");

        SocketManager socketManager = new SocketManager();
        SocketListener socketListener = new SocketListener(socketManager);
        GraphDeployer graphDeployer= new GraphDeployer(socketManager);
        
        
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(socketListener::run);
        executorService.submit(graphDeployer);
    }
}