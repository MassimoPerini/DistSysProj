package supervisor.communication;

import operator.types.Sum;
import org.jetbrains.annotations.NotNull;
import supervisor.communication.message.HeartbeatRequest;
import supervisor.communication.message.OperatorDeployment;
import utils.Debug;

import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by massimo 10/02/2018
 * This class rapresents the node of the network
 */
public class NodeSocket {
    //this is the socket of the operator's daemon
    private final TaskSocket daemonSocket;
    //this is the list of all operators associated to the node
    //they can be 0..n operations diffrent from each others
    private final List<TaskSocket> operatorsSocket;
    private final ExecutorService executorService;
    private static final int DELAY = 5000;

    NodeSocket(@NotNull TaskSocket daemonSocket)
    {
        this.daemonSocket = daemonSocket;
        this.operatorsSocket = Collections.synchronizedList(new ArrayList<TaskSocket>());
        this.executorService = Executors.newCachedThreadPool();
    }

    public void addOperatorSocket(@NotNull TaskSocket taskSocket)
    {
        operatorsSocket.add(taskSocket);
    }

    public void doHearbeat()
    {
        Timer [] timers = new Timer [operatorsSocket.size()+1];

        for (int i=0;i<timers.length;i++)
        {
            timers[i] = new Timer(true);
            timers[i].schedule(new TimerTask() {
                @Override
                public void run()
                {
                    Debug.printVerbose("TIMER!!!!!!!!");
                }
            }, DELAY);

            if (i==0){
                executorService.submit(daemonSocket::listen);
            }
            else
            {
                executorService.submit(operatorsSocket.get(i-1)::listen);
            }
        }

        daemonSocket.send(new HeartbeatRequest(0));

        for (int i=0;i<operatorsSocket.size();i++)
        {
            operatorsSocket.get(i).send(new HeartbeatRequest(i+1));
        }
    }

    //todo update using ip-port identifier
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeSocket that = (NodeSocket) o;

        if (daemonSocket != null ? !daemonSocket.equals(that.daemonSocket) : that.daemonSocket != null) return false;
        if (operatorsSocket != null ? !operatorsSocket.equals(that.operatorsSocket) : that.operatorsSocket != null)
            return false;
        return executorService != null ? executorService.equals(that.executorService) : that.executorService == null;
    }

    @Override
    public int hashCode() {
        int result = daemonSocket != null ? daemonSocket.hashCode() : 0;
        result = 31 * result + (operatorsSocket != null ? operatorsSocket.hashCode() : 0);
        result = 31 * result + (executorService != null ? executorService.hashCode() : 0);
        return result;
    }

    void deployOperator(OperatorDeployment operatorDeployment)
    {
        daemonSocket.send(operatorDeployment);
    }


}
