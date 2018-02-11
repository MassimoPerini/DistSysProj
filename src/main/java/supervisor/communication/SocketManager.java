package supervisor.communication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by massimo on 10/02/18.
 */
public class SocketManager {

    private final List<NodeSocket> nodeSocketList;
    private final static int TIME_BETWEEN_HEARTBEAT = 4000;

    public SocketManager()
    {
        nodeSocketList = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * this method adds a nodeSocket to the socket manager
     * @param nodeSocket it's the socket of a node
     */
    void addSocket(NodeSocket nodeSocket)
    {
        //TODO check if the current socket we want to put in is already there, and avoid equal!!!
        this.nodeSocketList.add(nodeSocket);
    }

    /**
     * This method is called when the system is ready to start the HeartBeat.
     * It spawns a new thread that do the hearthbeat for each node currently listed in.
     */
    public void startHeartBeat()
    {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            while (true) {
                for (NodeSocket nodeSocket : nodeSocketList) {
                    nodeSocket.doHearbeat();
                }
                Thread.sleep(TIME_BETWEEN_HEARTBEAT);
            }
        });
    }


}
