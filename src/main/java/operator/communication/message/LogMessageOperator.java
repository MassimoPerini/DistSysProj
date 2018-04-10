package operator.communication.message;

import supervisor.communication.message.MessageSupervisor;
import supervisor.communication.message.OperatorDeployment;
import supervisor.graph_representation.Graph;
import supervisor.graph_representation.Vertex;
import utils.Debug;

import java.util.List;

/**
 * Created by massimo on 10/04/18.
 */
public class LogMessageOperator implements MessageOperator {

    private final String message;

    public LogMessageOperator(String msg)
    {
        this.message = msg;
    }

    @Override
    public List<MessageSupervisor> execute(List<Vertex<OperatorDeployment>> sortedGraph) {
        Debug.printVerbose(this.message);
        return null;
    }
}
