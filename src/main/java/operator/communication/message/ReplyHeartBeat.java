package operator.communication.message;

import supervisor.Position;
import supervisor.communication.message.MessageSupervisor;
import supervisor.communication.message.OperatorDeployment;
import supervisor.graph_representation.Graph;
import supervisor.graph_representation.Vertex;
import utils.Debug;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by massimo on 10/02/18.
 */
public class ReplyHeartBeat implements MessageOperator, Serializable {

    private final List<Position> failedPositions;

    public ReplyHeartBeat(List<Position> failedPositions) {
        this.failedPositions = failedPositions;
    }

    @Override
    public List<MessageSupervisor> execute(List<Vertex<OperatorDeployment>> sortedGraph) {
        if (this.failedPositions.size() > 0){
            Debug.printVerbose("E' fallito almeno un processo");
        }

        List<MessageSupervisor> operatorDeploymentLaunch = new LinkedList<>();

        for (Position failedPosition : failedPositions) {
            Stream<Vertex<OperatorDeployment>> pippo = sortedGraph.stream().filter(d->d.getData().getOwnPosition().equals(failedPosition));
            Stream<OperatorDeployment> operatorDeploymentStream = pippo.map(d -> d.getData());
            OperatorDeployment operatorDeployment = operatorDeploymentStream.findFirst().orElse(null);
            operatorDeploymentLaunch.add(operatorDeployment);
        }

        return operatorDeploymentLaunch;
    }

    //metodo per navigare il grafo data una Position mi restituisca op deplo
}
