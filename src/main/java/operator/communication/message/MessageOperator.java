package operator.communication.message;

import supervisor.communication.message.MessageSupervisor;
import supervisor.communication.message.OperatorDeployment;
import supervisor.graph_representation.Graph;
import supervisor.graph_representation.Vertex;

import java.io.Serializable;
import java.util.List;

/**
 * Created by massimo on 10/02/18.
 */
public interface MessageOperator extends Serializable{
    List<MessageSupervisor> execute(List<Vertex<OperatorDeployment>> sortedGraph);
}
