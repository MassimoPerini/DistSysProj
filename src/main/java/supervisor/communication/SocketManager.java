package supervisor.communication;

import operator.types.Sum;
import supervisor.communication.message.OperatorDeployment;

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

    synchronized void addSocket(NodeSocket nodeSocket)
    {
        //todo check consistency with equals
        for (NodeSocket socket : nodeSocketList) {
            if(nodeSocket.equals(socket)){
                throw new IllegalArgumentException();
            }
        }
        this.nodeSocketList.add(nodeSocket);
    }

    /**
     * this method is called by the supervisor in order to start the heartbeat
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

    /**
     * This method deploys a new operator.
     * It can be reused to deploy more times different operators in the same node
     * @param nodeToDeploy this is related to fact 1.1. For the moment we handle the list of all nodes
     *                     controlled by the supervisor using a topological sort.
     * @param operatorDeployment type of opeation to deploy on the node
     */
    public void deployNewOperator(int nodeToDeploy, OperatorDeployment operatorDeployment)
    {
        nodeSocketList.get(nodeToDeploy).deployOperator(operatorDeployment);
    }

	public int getNumberOfCurrentlyConnectedDaemons() {
		return nodeSocketList.size();
	}
}