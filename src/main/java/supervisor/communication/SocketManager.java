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

    void addSocket(NodeSocket nodeSocket)
    {
        //TODO avoid equal!!!
        this.nodeSocketList.add(nodeSocket);
    }

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
