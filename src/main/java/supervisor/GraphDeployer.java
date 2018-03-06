package supervisor;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;


import com.google.gson.Gson;

import com.google.gson.reflect.TypeToken;
import supervisor.communication.SocketListener;
import supervisor.communication.SocketManager;
import supervisor.communication.message.OperatorDeployment;
import supervisor.graph_representation.Graph;
import supervisor.graph_representation.Vertex;
import utils.CliPrinter;
import utils.Debug;

/**
 * This class contains a socketListener to list all the available daemons.
 * Upon user request, it deploys the graph given in input over the aforementioned daemons.
 */
public class GraphDeployer implements Runnable{
	
	/**
	 * The available daemons
	 */
	private SocketManager socketManager;
	
	/**
	 * @param manager the component in charge of waiting for daemons to connect
	 */
	public GraphDeployer(SocketManager manager)
	{
		this.socketManager=manager;
	}
	
	/**
	 * Wait for the user to input a graph; then, deploy it over the available daemons
	 * Currently, the graph must be input in the form of a json text (eg, {"vertices":{}} )
	 */
	@Override
	public void run() {
        Scanner scanner=new Scanner(System.in);
        Debug.printVerbose("Waiting for JSON input");
        String iString= scanner.nextLine();
		Debug.printVerbose("JSON received");
		Type fooType = new TypeToken<Graph<OperatorDeployment>>() {}.getType();
		Debug.printVerbose("Token generated");
		Graph<OperatorDeployment> graph= new Gson().fromJson(iString, fooType);
		Debug.printVerbose("GSON processed" + graph.toString());
		List<Vertex<OperatorDeployment>> sortedGraph=graph.topologicalSort();
        if(sortedGraph!=null)
        {
        	try {
				deploy(sortedGraph);
				Debug.printVerbose("Deployed sorted graph");
			} catch (NoDaemonAvailableException e) {
				Debug.printError("No daemon is currently available");
			}
        }
        else
        {
        	Debug.printError("Cyclic graph");
        }
		scanner.close();
	}
	
	/**
	 * Deploy a list of vertex. 
	 * @param graph	nodes must be ordered. In particular, first mustn't have any incoming edge, last must have 
	 * no outgoing edge
	 */
	public void deploy (List<Vertex<OperatorDeployment>> graph) throws NoDaemonAvailableException
	{
		if(this.socketManager.getNumberOfCurrentlyConnectedDaemons()==0)
		{
			throw new NoDaemonAvailableException(); 
		}
		//Reverses the topological order to simplify forward star implementation
		Collections.reverse(graph);
		for(Vertex<OperatorDeployment> curr:graph)
		{
			int position=new Random().nextInt(this.socketManager.getNumberOfCurrentlyConnectedDaemons());
			this.socketManager.deployNewOperator(position, curr.getData());
		}
	}

}
